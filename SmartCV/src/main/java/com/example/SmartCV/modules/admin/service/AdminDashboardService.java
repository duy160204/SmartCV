package com.example.SmartCV.modules.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.admin.dto.AdminDashboardResponse;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.cv.repository.CVRepository;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionStatus;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CVRepository cvRepository;
    private final TemplateRepository templateRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public AdminDashboardResponse getDashboardOverview() {

        long totalUsers = userRepository.count();
        long verifiedUsers = userRepository.countByVerifiedTrue();
        long lockedUsers = userRepository.countByLockedTrue();

        long totalCVs = cvRepository.count();
        long publicCVs = cvRepository.countByIsPublicTrue();

        long freeUsers = userSubscriptionRepository.countByPlan(PlanType.FREE);
        long proUsers = userSubscriptionRepository.countByPlan(PlanType.PRO);
        long premiumUsers = userSubscriptionRepository.countByPlan(PlanType.PREMIUM);

        long totalTemplates = templateRepository.count();
        long activeTemplates = templateRepository.countByIsActiveTrue();

        long totalSubscriptionsActive = userSubscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .verifiedUsers(verifiedUsers)
                .lockedUsers(lockedUsers)
                .totalCVs(totalCVs)
                .publicCVs(publicCVs)
                .freeUsers(freeUsers)
                .proUsers(proUsers)
                .premiumUsers(premiumUsers)
                .totalTemplates(totalTemplates)
                .activeTemplates(activeTemplates)
                .totalSubscriptionsActive(totalSubscriptionsActive)
                .build();
    }
}
