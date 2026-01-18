package com.example.SmartCV.modules.subscription.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.service.EmailService;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.repository.CVRepository;
import com.example.SmartCV.modules.subscription.domain.*;
import com.example.SmartCV.modules.subscription.dto.MySubscriptionDTO;
import com.example.SmartCV.modules.subscription.dto.PublicLinkResponseDTO;
import com.example.SmartCV.modules.subscription.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PlanDefinitionRepository planDefinitionRepository;
    private final SubscriptionUsageRepository subscriptionUsageRepository;
    private final PlanFeatureRepository planFeatureRepository;
    private final SubscriptionHistoryService subscriptionHistoryService;

    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.public-cv-url:http://localhost:3000}")
    private String publicCvUrl;

    // =========================
    // INIT FREE SUB (KHI REGISTER)
    // =========================
    public void initFreeSubscription(Long userId) {

        if (userSubscriptionRepository.existsByUserId(userId))
            return;

        UserSubscription sub = UserSubscription.builder()
                .userId(userId)
                .plan(PlanType.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .build();

        userSubscriptionRepository.save(sub);
    }

    // =========================
    // GET ACTIVE SUB
    // =========================
    public UserSubscription getActiveSubscription(Long userId) {

        return userSubscriptionRepository.findByUserId(userId)
                .filter(UserSubscription::isActive)
                .orElseThrow(() -> new RuntimeException("User has no active subscription"));
    }

    // =========================
    // GET MY SUB INFO
    // =========================
    public MySubscriptionDTO getMySubscriptionInfo(Long userId) {

        UserSubscription sub = getActiveSubscription(userId);

        return new MySubscriptionDTO(
                sub.getPlan(),
                sub.getStatus(),
                sub.getStartDate(),
                sub.getEndDate());
    }

    // =========================
    // PUBLIC CV (SHARE)
    // =========================
    public PublicLinkResponseDTO publicCV(Long userId, Long cvId) {

        UserSubscription sub = getActiveSubscription(userId);
        PlanDefinition planDef = getPlanDefinition(sub.getPlan());

        CV cv = getOwnedCV(userId, cvId);

        if (isAlreadyShared(userId, cvId)) {
            throw new RuntimeException("This CV is already public");
        }

        checkShareQuota(userId, planDef);

        SubscriptionUsage usage = createShareUsage(
                userId,
                cvId,
                sub.getPlan(),
                planDef);

        sendMailPublicLink(userId, usage.getShareUuid(), usage.getExpireAt());

        return new PublicLinkResponseDTO(
                buildPublicUrl(usage.getShareUuid()),
                usage.getExpireAt().toString());
    }

    private void checkShareQuota(Long userId, PlanDefinition planDef) {

        long used = subscriptionUsageRepository
                .countByUserIdAndUsageTypeAndPeriod(
                        userId,
                        UsageType.SHARE,
                        currentPeriod());

        if (used >= planDef.getMaxSharePerMonth()) {
            throw new RuntimeException("Share quota exceeded for current plan");
        }
    }

    // =========================
    // DOWNLOAD PERMISSION
    // =========================
    public void checkDownloadPermission(Long userId) {

        UserSubscription sub = getActiveSubscription(userId);

        boolean allowed = planFeatureRepository
                .findByPlanAndFeatureCode(sub.getPlan(), "DOWNLOAD_CV")
                .map(PlanFeature::isEnabled)
                .orElse(false);

        if (!allowed) {
            throw new RuntimeException("Your plan does not allow downloading CV");
        }
    }

    // =========================
    // CREATE CV PERMISSION
    // =========================
    public void checkCanCreateCV(Long userId) {

        UserSubscription sub = getActiveSubscription(userId);

        boolean allowed = planFeatureRepository
                .findByPlanAndFeatureCode(sub.getPlan(), "CREATE_CV")
                .map(PlanFeature::isEnabled)
                .orElse(false);

        if (!allowed) {
            throw new RuntimeException("Your plan does not allow creating CV");
        }
    }

    // =========================
    // RECORD DOWNLOAD USAGE
    // =========================
    public void recordDownload(Long userId, Long cvId) {

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .userId(userId)
                .cvId(cvId)
                .usageType(UsageType.DOWNLOAD)
                .period(currentPeriod())
                .createdAt(LocalDateTime.now())
                .build();

        subscriptionUsageRepository.save(usage);
    }

    // =========================
    // REVOKE PUBLIC LINK (MANUAL)
    // =========================
    public void revokePublicLink(Long userId, Long cvId) {

        SubscriptionUsage usage = subscriptionUsageRepository
                .findByUserIdAndCvIdAndUsageType(userId, cvId, UsageType.SHARE)
                .orElseThrow(() -> new RuntimeException("Public link not found"));

        sendMailRevoked(userId, usage.getShareUuid());
        subscriptionUsageRepository.delete(usage);
    }

    // =========================
    // CRON – EXPIRE + NOTIFY
    // =========================
    public void handleExpireAndNotify() {

        LocalDateTime now = LocalDateTime.now();

        List<SubscriptionUsage> toNotify = subscriptionUsageRepository
                .findByExpireAtBeforeAndNotifiedBeforeExpireFalse(
                        now.plusDays(5));

        for (SubscriptionUsage usage : toNotify) {
            notifyBeforeExpire(usage);
            usage.setNotifiedBeforeExpire(true);
            subscriptionUsageRepository.save(usage);
        }

        List<SubscriptionUsage> expired = subscriptionUsageRepository.findByExpireAtBefore(now);

        for (SubscriptionUsage usage : expired) {
            revokeAndNotifyExpired(usage);
        }
    }

    private void notifyBeforeExpire(SubscriptionUsage usage) {

        User user = userRepository.findById(usage.getUserId()).orElse(null);
        if (user == null)
            return;

        emailService.sendPublicLinkExpiringSoonEmail(
                user.getEmail(),
                usage.getShareUuid(),
                usage.getExpireAt());
    }

    private void revokeAndNotifyExpired(SubscriptionUsage usage) {

        User user = userRepository.findById(usage.getUserId()).orElse(null);

        if (user != null) {
            emailService.sendPublicLinkExpiredEmail(
                    user.getEmail(),
                    usage.getShareUuid());
        }

        subscriptionUsageRepository.delete(usage);
    }

    // =========================
    // ACTIVATE SUBSCRIPTION (AUTO)
    // =========================
    public void activateSubscription(com.example.SmartCV.modules.payment.domain.PaymentTransaction tx) {

        if (tx.getStatus() != com.example.SmartCV.modules.payment.domain.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment is not successful");
        }

        PlanDefinition planDef = planDefinitionRepository
                .findByPlanAndDurationMonths(tx.getPlan(), tx.getMonths())
                .orElseThrow(() -> new RuntimeException(
                        "Plan not found for type=" + tx.getPlan() + " months=" + tx.getMonths()));

        UserSubscription sub = userSubscriptionRepository.findByUserId(tx.getUserId())
                .orElseThrow(() -> new RuntimeException("User subscription not found"));

        PlanType oldPlanType = sub.getPlan();

        // --- AUDIT HISTORY START ---
        // Use available Enums: PAYMENT_SUCCESS covers New, Renew, Upgrade triggered by
        // payment.
        SubscriptionChangeType changeType = SubscriptionChangeType.PAYMENT_SUCCESS;
        // --- AUDIT HISTORY END ---

        LocalDate start = LocalDate.now();
        LocalDate end;

        // LIFECYCLE LOGIC:
        // 1. Same Plan (Renewal) -> Extend endDate
        // 2. Different Plan (Upgrade/Downgrade/New) -> New Start Date
        if (sub.isActive() && sub.getPlan() == tx.getPlan()) {
            LocalDate baseDate = sub.getEndDate().isBefore(start) ? start : sub.getEndDate();
            end = baseDate.plusDays(planDef.getDurationDays());
        } else {
            // OVERRIDE
            sub.setStartDate(start);
            end = start.plusDays(planDef.getDurationDays());
        }

        sub.setPlan(tx.getPlan());
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setEndDate(end);

        userSubscriptionRepository.save(sub);

        // RECORD HISTORY
        subscriptionHistoryService.saveHistory(
                tx.getUserId(),
                oldPlanType,
                tx.getPlan(),
                changeType,
                ChangeReason.PAYMENT,
                tx.getId(),
                null);

        // Notify
        User user = userRepository.findById(tx.getUserId()).orElse(null);
        if (user != null) {
            emailService.sendPlanUpdatedEmail(
                    user.getEmail(),
                    oldPlanType.name(),
                    tx.getPlan().name());
        }
    }

    // =========================
    // ADMIN – UPDATE PLAN (HỆ THỐNG TÍNH NGÀY)
    // =========================
    public void updatePlanManually(Long userId, PlanType newPlan) {

        UserSubscription sub = userSubscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User subscription not found"));

        PlanType oldPlan = sub.getPlan();

        PlanDefinition planDef = getPlanDefinition(newPlan);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(planDef.getDurationDays());

        sub.setPlan(newPlan);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartDate(start);
        sub.setEndDate(end);

        userSubscriptionRepository.save(sub);

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            emailService.sendPlanUpdatedEmail(
                    user.getEmail(),
                    oldPlan.name(),
                    newPlan.name());
        }
    }

    // =========================
    // HELPERS
    // =========================
    private PlanDefinition getPlanDefinition(PlanType plan) {

        return planDefinitionRepository.findByPlan(plan)
                .orElseThrow(() -> new RuntimeException("Plan definition not found"));
    }

    private CV getOwnedCV(Long userId, Long cvId) {

        return cvRepository.findById(cvId)
                .filter(cv -> cv.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("CV not found or not owned"));
    }

    private boolean isAlreadyShared(Long userId, Long cvId) {

        return subscriptionUsageRepository
                .existsByUserIdAndCvIdAndUsageType(
                        userId,
                        cvId,
                        UsageType.SHARE);
    }

    private SubscriptionUsage createShareUsage(
            Long userId,
            Long cvId,
            PlanType plan,
            PlanDefinition planDef) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusDays(planDef.getPublicLinkExpireDays());

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .userId(userId)
                .cvId(cvId)
                .plan(plan)
                .usageType(UsageType.SHARE)
                .shareUuid(UUID.randomUUID().toString())
                .createdAt(now)
                .expireAt(expireAt)
                .period(currentPeriod())
                .notifiedBeforeExpire(false)
                .build();

        return subscriptionUsageRepository.save(usage);
    }

    private String currentPeriod() {
        return YearMonth.now().toString(); // yyyy-MM
    }

    private String buildPublicUrl(String uuid) {
        return publicCvUrl + "/public/" + uuid;
    }

    private void sendMailPublicLink(Long userId, String uuid, LocalDateTime expireAt) {

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            emailService.sendPublicLinkEmail(user.getEmail(), uuid, expireAt);
        }
    }

    private void sendMailRevoked(Long userId, String uuid) {

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            emailService.sendPublicLinkRevokedEmail(user.getEmail(), uuid);
        }
    }
}
