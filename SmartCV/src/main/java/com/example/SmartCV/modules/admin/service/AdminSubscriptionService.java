package com.example.SmartCV.modules.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.admin.dto.SubscriptionConfirmRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewResponse;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.service.EmailService;
import com.example.SmartCV.modules.subscription.domain.*;
import com.example.SmartCV.modules.subscription.repository.SubscriptionHistoryRepository;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;
import com.example.SmartCV.modules.subscription.service.SubscriptionCalculationService;
import com.example.SmartCV.modules.subscription.service.SubscriptionHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSubscriptionService {

        private static final Logger log = LoggerFactory.getLogger(AdminSubscriptionService.class);

        private final UserSubscriptionRepository userSubscriptionRepository;
        private final SubscriptionHistoryRepository historyRepository; // 🔥 Added for idempotency
        private final UserRepository userRepository;
        private final SubscriptionCalculationService calculationService;
        private final SubscriptionHistoryService historyService;
        private final EmailService emailService;

        // =========================
        // PREVIEW – chỉ tính, KHÔNG ghi DB
        // =========================
        @Transactional(readOnly = true)
        public SubscriptionPreviewResponse preview(SubscriptionPreviewRequest request) {

                Long userId = request.getUserId();
                PlanType newPlan = request.getNewPlan();
                int months = request.getDurationMonths();

                // [VALIDATION] Essential safety
                if (months <= 0) {
                        throw new IllegalArgumentException("Duration must be at least 1 month.");
                }

                UserSubscription currentSub = userSubscriptionRepository.findByUserId(userId).orElse(null);

                PlanType oldPlan = currentSub != null ? currentSub.getPlan() : null;

                validatePlanChange(oldPlan, newPlan);

                SubscriptionPeriod period = calculationService.calculatePeriod(
                                currentSub,
                                newPlan,
                                months);

                log.info(
                                "[ADMIN][PREVIEW] userId={} oldPlan={} newPlan={} months={} start={} end={}",
                                userId,
                                oldPlan,
                                newPlan,
                                months,
                                period.getStartDate(),
                                period.getEndDate());

                return SubscriptionPreviewResponse.builder()
                                .userId(userId)
                                .oldPlan(oldPlan)
                                .newPlan(newPlan)
                                .startDate(period.getStartDate())
                                .endDate(period.getEndDate())
                                .message(buildPreviewMessage(oldPlan, newPlan, period))
                                .build();
        }

        // =========================
        // CONFIRM – UPDATE THẬT
        // =========================
        public void confirm(
                        Long adminId,
                        SubscriptionConfirmRequest request) {

                if (!request.isConfirm()) {
                        throw new RuntimeException("Confirmation is required");
                }

                Long userId = request.getUserId();
                PlanType newPlan = request.getNewPlan();
                int months = request.getDurationMonths();

                User user = getUserOrThrow(userId);

                UserSubscription currentSub = userSubscriptionRepository.findByUserId(userId).orElse(null);

                PlanType oldPlan = currentSub != null ? currentSub.getPlan() : null;

                // [IDEMPOTENCY] Crucial check: Has this specific payment already been credited?
                // Using both Repository check AND DB Unique Constraint for maximum safety.
                if (request.getPaymentId() != null && historyRepository.existsByPaymentId(request.getPaymentId())) {
                        log.info("[ADMIN][CONFIRM] Payment {} already processed (checked via repo). Skipping.", request.getPaymentId());
                        return;
                }

                validatePlanChange(oldPlan, newPlan);

                SubscriptionPeriod period = calculationService.calculatePeriod(
                                currentSub,
                                newPlan,
                                months);

                // ===== UPSERT UserSubscription =====
                UserSubscription updatedSub = upsertSubscription(
                                currentSub,
                                userId,
                                newPlan,
                                period,
                                request.getPaymentId()); // Pass paymentId for trace

                userSubscriptionRepository.save(updatedSub);

                // ===== SAVE HISTORY (DB UNIQUE CONSTRAINT SAFE) =====
                try {
                        historyService.saveHistory(
                                        userId,
                                        oldPlan,
                                        newPlan,
                                        SubscriptionChangeType.ADMIN_UPDATE,
                                        ChangeReason.ADMIN,
                                        request.getPaymentId(),
                                        adminId
                        );
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("[ADMIN][CONFIRM] Race condition detected for paymentId {}. History already exists.", request.getPaymentId());
                        // If history already exists, it means another thread just finished this confirm.
                        // Since we are in @Transactional, this will trigger rollback properly if needed,
                        // but since we caught it, we can just return.
                        return; 
                }

                // ===== SEND EMAIL =====
                emailService.sendPlanUpdatedEmail(
                                user.getEmail(),
                                oldPlan != null ? oldPlan.name() : "NONE",
                                newPlan.name());

                log.info(
                                "[ADMIN][CONFIRM] Success for userId={} newPlan={} paymentId={}",
                                userId,
                                newPlan,
                                request.getPaymentId());
        }

        // =========================
        // PRIVATE HELPERS
        // =========================

        private UserSubscription upsertSubscription(
                        UserSubscription currentSub,
                        Long userId,
                        PlanType newPlan,
                        SubscriptionPeriod period,
                        Long paymentId) {
                
                // [ENSURE EXISTS] New requirement: UserSubscription MUST always exist
                UserSubscription sub = currentSub;
                if (sub == null) {
                        sub = UserSubscription.builder()
                                        .userId(userId)
                                        .plan(PlanType.FREE) // Start at FREE if brand new 
                                        .status(SubscriptionStatus.ACTIVE)
                                        .startDate(java.time.LocalDate.now())
                                        .build();
                }

                sub.setPlan(newPlan);
                sub.setStatus(SubscriptionStatus.ACTIVE);
                sub.setStartDate(period.getStartDate());
                sub.setEndDate(period.getEndDate());
                sub.setLastPaymentId(paymentId); // Trace paymentId in sub as well
                
                return sub;
        }

        private User getUserOrThrow(Long userId) {
                return userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        }

        private void validatePlanChange(PlanType oldPlan, PlanType newPlan) {

                if (newPlan == null) {
                        throw new RuntimeException("New plan must not be null");
                }

                // [STRICT RULE] FREE plan is not creatable via admin/payment flow
                if (newPlan == PlanType.FREE) {
                    throw new RuntimeException("FREE plan cannot be requested via paid workflow");
                }

                // [STRICT RULE] Prevent PREMIUM -> FREE downgrade
                if (oldPlan == PlanType.PREMIUM && newPlan == PlanType.FREE) {
                    throw new RuntimeException("Cannot downgrade from PREMIUM to FREE via admin request.");
                }

                if (oldPlan != null && oldPlan == newPlan) {
                    // This is still a valid check for NEW activation requests.
                    // But if it's a SAME PLAN EXTENSION, calculationService handles it.
                    // The business requirement says "Same plan -> extend". 
                    // So we should NOT throw exception if oldPlan == newPlan, 
                    // unless it's a logic error in the flow.
                    // Actually, for Admin Confirm flow, same plan extension is EXPECTED.
                    log.debug("Extending same plan: {}", newPlan);
                }
        }

        private String buildPreviewMessage(
                        PlanType oldPlan,
                        PlanType newPlan,
                        SubscriptionPeriod period) {
                String from = oldPlan != null ? oldPlan.name() : "NONE";
                return String.format(
                                "Change from %s → %s (%s → %s)",
                                from,
                                newPlan.name(),
                                period.getStartDate(),
                                period.getEndDate());
        }
}
