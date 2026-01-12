package com.example.SmartCV.modules.admin.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.admin.dto.SubscriptionConfirmRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewResponse;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.subscription.domain.ChangeReason;
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionChangeType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionPeriod;
import com.example.SmartCV.modules.subscription.domain.SubscriptionStatus;
import com.example.SmartCV.modules.subscription.domain.UserSubscription;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;
import com.example.SmartCV.modules.subscription.service.SubscriptionCalculationService;
import com.example.SmartCV.modules.subscription.service.SubscriptionHistoryService;
import com.example.SmartCV.modules.auth.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(AdminSubscriptionService.class);

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionCalculationService calculationService;
    private final SubscriptionHistoryService historyService;
    private final EmailService emailService;

    // =========================
    // PREVIEW – chỉ tính, không update
    // =========================
    @Transactional(readOnly = true)
    public SubscriptionPreviewResponse preview(SubscriptionPreviewRequest request) {

        Long userId = request.getUserId();
        PlanType newPlan = request.getNewPlan();

        User user = getUserOrThrow(userId);
        UserSubscription currentSub = userSubscriptionRepository.findByUserId(userId).orElse(null);

        PlanType oldPlan = currentSub != null ? currentSub.getPlan() : null;

        validatePlanChange(oldPlan, newPlan);

        SubscriptionPeriod period = calculationService.calculatePeriod(currentSub, newPlan);

        String message = buildPreviewMessage(oldPlan, newPlan, period);

        log.info("[ADMIN][PREVIEW] userId={} oldPlan={} newPlan={} start={} end={}",
                userId, oldPlan, newPlan, period.getStartDate(), period.getEndDate());

        return SubscriptionPreviewResponse.builder()
                .userId(userId)
                .oldPlan(oldPlan)
                .newPlan(newPlan)
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .message(message)
                .build();
    }

    // =========================
    // CONFIRM – update thật
    // =========================
    public void confirm(SubscriptionConfirmRequest request) {

        if (!request.isConfirm()) {
            throw new RuntimeException("Confirmation is required to update subscription");
        }

        Long userId = request.getUserId();
        PlanType newPlan = request.getNewPlan();

        User user = getUserOrThrow(userId);
        UserSubscription currentSub = userSubscriptionRepository.findByUserId(userId).orElse(null);

        PlanType oldPlan = currentSub != null ? currentSub.getPlan() : null;

        validatePlanChange(oldPlan, newPlan);

        SubscriptionPeriod period = calculationService.calculatePeriod(currentSub, newPlan);

        // ===== Update UserSubscription =====
        if (currentSub == null) {
            currentSub = UserSubscription.builder()
                    .userId(userId)
                    .plan(newPlan)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(period.getStartDate())
                    .endDate(period.getEndDate())
                    .build();
        } else {
            currentSub.setPlan(newPlan);
            currentSub.setStatus(SubscriptionStatus.ACTIVE);
            currentSub.setStartDate(period.getStartDate());
            currentSub.setEndDate(period.getEndDate());
        }

        userSubscriptionRepository.save(currentSub);

        // ===== Save History =====
        historyService.saveHistory(
                userId,
                oldPlan,
                newPlan,
                SubscriptionChangeType.ADMIN_UPDATE,
                ChangeReason.ADMIN
        );

        // ===== Send Mail =====
        emailService.sendPlanUpdatedEmail(
                user.getEmail(),
                oldPlan != null ? oldPlan.name() : "NONE",
                newPlan.name()
        );

        // ===== Log =====
        log.info("[ADMIN][CONFIRM] userId={} oldPlan={} newPlan={} start={} end={}",
                userId, oldPlan, newPlan, period.getStartDate(), period.getEndDate());
    }

    // =========================
    // PRIVATE HELPERS
    // =========================

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id = " + userId));
    }

    private void validatePlanChange(PlanType oldPlan, PlanType newPlan) {

        if (newPlan == null) {
            throw new RuntimeException("New plan must not be null");
        }

        if (oldPlan != null && oldPlan == newPlan) {
            throw new RuntimeException("User is already in plan: " + newPlan);
        }
    }

    private String buildPreviewMessage(PlanType oldPlan, PlanType newPlan, SubscriptionPeriod period) {

        String from = oldPlan != null ? oldPlan.name() : "NONE";
        String to = newPlan.name();

        return String.format(
                "User will be changed from %s to %s. Period: %s -> %s",
                from,
                to,
                period.getStartDate(),
                period.getEndDate()
        );
    }
}
