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

    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.public-cv-url:http://localhost:3000}")
    private String publicCvUrl;

    // =========================
    // INIT FREE SUB (CALL KHI REGISTER)
    // =========================
    public void initFreeSubscription(Long userId) {

        if (userSubscriptionRepository.existsByUserId(userId)) return;

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
                sub.getEndDate()
        );
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
                planDef
        );

        sendMailPublicLink(userId, usage.getShareUuid(), usage.getExpireAt());

        return new PublicLinkResponseDTO(
                buildPublicUrl(usage.getShareUuid()),
                usage.getExpireAt().toString()
        );
    }

    private void checkShareQuota(Long userId, PlanDefinition planDef) {

        long used = subscriptionUsageRepository
                .countByUserIdAndUsageTypeAndPeriod(
                        userId,
                        UsageType.SHARE,
                        currentPeriod()
                );

        if (used >= planDef.getMaxSharePerMonth()) {
            throw new RuntimeException("Share quota exceeded for current plan");
        }
    }

    // =========================
    // DOWNLOAD PERMISSION (NPS)
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

    // ❗ Không check quota vì PlanDefinition chưa quản lý download limit
    }

    // =========================
// CHECK CREATE CV PERMISSION
// =========================
// =========================
// CHECK CREATE CV PERMISSION
// =========================
// =========================
// CHECK CREATE CV PERMISSION
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

        // ❗ Không check số lượng ở đây
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

        List<SubscriptionUsage> toNotify =
                subscriptionUsageRepository
                        .findByExpireAtBeforeAndNotifiedBeforeExpireFalse(
                                now.plusDays(5)
                        );

        for (SubscriptionUsage usage : toNotify) {
            notifyBeforeExpire(usage);
            usage.setNotifiedBeforeExpire(true);
            subscriptionUsageRepository.save(usage);
        }

        List<SubscriptionUsage> expired =
                subscriptionUsageRepository.findByExpireAtBefore(now);

        for (SubscriptionUsage usage : expired) {
            revokeAndNotifyExpired(usage);
        }
    }

    private void notifyBeforeExpire(SubscriptionUsage usage) {

        User user = userRepository.findById(usage.getUserId()).orElse(null);
        if (user == null) return;

        emailService.sendPublicLinkExpiringSoonEmail(
                user.getEmail(),
                usage.getShareUuid(),
                usage.getExpireAt()
        );
    }

    private void revokeAndNotifyExpired(SubscriptionUsage usage) {

        User user = userRepository.findById(usage.getUserId()).orElse(null);

        if (user != null) {
            emailService.sendPublicLinkExpiredEmail(
                    user.getEmail(),
                    usage.getShareUuid()
            );
        }

        subscriptionUsageRepository.delete(usage);
    }

    // =========================
    // ADMIN – UPDATE PLAN
    // =========================
    public void updatePlanManually(Long userId, PlanType newPlan, int months) {

        UserSubscription sub = userSubscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User subscription not found"));

        sub.setPlan(newPlan);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusMonths(months));

        userSubscriptionRepository.save(sub);

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            emailService.sendPlanUpdatedEmail(user.getEmail(), newPlan.name());
        }
    }

    // =========================
    // HELPERS (PRIVATE)
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
                        UsageType.SHARE
                );
    }

    private SubscriptionUsage createShareUsage(
            Long userId,
            Long cvId,
            PlanType plan,
            PlanDefinition planDef
    ) {

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
