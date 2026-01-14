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
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;
import com.example.SmartCV.modules.subscription.service.SubscriptionCalculationService;
import com.example.SmartCV.modules.subscription.service.SubscriptionHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSubscriptionService {

    private static final Logger log =
            LoggerFactory.getLogger(AdminSubscriptionService.class);

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionCalculationService calculationService;
    private final SubscriptionHistoryService historyService;
    private final EmailService emailService;

    // =========================
    // PREVIEW – chỉ tính, không ghi DB
    // =========================
    @Transactional(readOnly = true)
    public SubscriptionPreviewResponse preview(SubscriptionPreviewRequest request) {

        Long userId = request.getUserId();
        PlanType newPlan = request.getNewPlan();

        UserSubscription currentSub =
                userSubscriptionRepository.findByUserId(userId).orElse(null);

        PlanType oldPlan = currentSub != null ? currentSub.getPlan() : null;

        validatePlanChange(oldPlan, newPlan);

        SubscriptionPeriod period =
                calculationService.calculatePeriod(currentSub, newPlan);

        log.info(
            "[ADMIN][PREVIEW] userId={} oldPlan={} newPlan={} start={} end={}",
            userId,
            oldPlan,
            newPlan,
            period.getStartDate(),
            period.getEndDate()
        );

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
    // CONFIRM – update thật
    // =========================
    public void confirm(
            Long adminId,
            SubscriptionConfirmRequest request
    ) {

        if (!request.isConfirm()) {
            throw new RuntimeException("Confirmation is required");
        }

        Long userId = request.getUserId();
        PlanType newPlan = request.getNewPlan();

        User user = getUserOrThrow(userId);

        UserSubscription currentSub =
                userSubscriptionRepository.findByUserId(userId).orElse(null);

        PlanType oldPlan = currentSub != null ? currentSub.getPlan() : null;

        validatePlanChange(oldPlan, newPlan);

        SubscriptionPeriod period =
                calculationService.calculatePeriod(currentSub, newPlan);

        // ===== UPSERT UserSubscription =====
        UserSubscription updatedSub =
                upsertSubscription(currentSub, userId, newPlan, period);

        userSubscriptionRepository.save(updatedSub);

        // ===== SAVE HISTORY (FIX LỖI Ở ĐÂY) =====
        historyService.saveHistory(
                userId,
                oldPlan,
                newPlan,
                SubscriptionChangeType.ADMIN_UPDATE,
                ChangeReason.ADMIN,
                adminId,   // 👈 operator
                null       // 👈 paymentId (admin thao tác)
        );

        // ===== SEND EMAIL =====
        emailService.sendPlanUpdatedEmail(
                user.getEmail(),
                oldPlan != null ? oldPlan.name() : "NONE",
                newPlan.name()
        );

        log.info(
            "[ADMIN][CONFIRM] adminId={} userId={} oldPlan={} newPlan={} start={} end={}",
            adminId,
            userId,
            oldPlan,
            newPlan,
            period.getStartDate(),
            period.getEndDate()
        );
    }

    // =========================
    // PRIVATE HELPERS
    // =========================

    private UserSubscription upsertSubscription(
            UserSubscription currentSub,
            Long userId,
            PlanType newPlan,
            SubscriptionPeriod period
    ) {
        if (currentSub == null) {
            return UserSubscription.builder()
                    .userId(userId)
                    .plan(newPlan)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(period.getStartDate())
                    .endDate(period.getEndDate())
                    .build();
        }

        currentSub.setPlan(newPlan);
        currentSub.setStatus(SubscriptionStatus.ACTIVE);
        currentSub.setStartDate(period.getStartDate());
        currentSub.setEndDate(period.getEndDate());
        return currentSub;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + userId));
    }

    private void validatePlanChange(PlanType oldPlan, PlanType newPlan) {

        if (newPlan == null) {
            throw new RuntimeException("New plan must not be null");
        }

        if (oldPlan != null && oldPlan == newPlan) {
            throw new RuntimeException(
                "User is already on plan: " + newPlan
            );
        }
    }

    private String buildPreviewMessage(
            PlanType oldPlan,
            PlanType newPlan,
            SubscriptionPeriod period
    ) {
        String from = oldPlan != null ? oldPlan.name() : "NONE";
        return String.format(
            "Change from %s → %s (%s → %s)",
            from,
            newPlan.name(),
            period.getStartDate(),
            period.getEndDate()
        );
    }
}
