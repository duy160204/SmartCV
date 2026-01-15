package com.example.SmartCV.modules.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.admin.dto.AdminDashboardResponse;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.cv.repository.CVRepository;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
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
    private final PaymentTransactionRepository paymentRepository;

    public AdminDashboardResponse getDashboardOverview() {

        // =========================
        // USER
        // =========================
        long totalUsers = userRepository.count();
        long verifiedUsers = userRepository.countByVerifiedTrue();
        long lockedUsers = userRepository.countByLockedTrue();

        // =========================
        // CV
        // =========================
        long totalCVs = cvRepository.count();
        long publicCVs = cvRepository.countByIsPublicTrue();

        // =========================
        // SUBSCRIPTION
        // =========================
        long freeUsers =
                userSubscriptionRepository.countByPlan(PlanType.FREE);
        long proUsers =
                userSubscriptionRepository.countByPlan(PlanType.PRO);
        long premiumUsers =
                userSubscriptionRepository.countByPlan(PlanType.PREMIUM);

        long totalSubscriptionsActive =
                userSubscriptionRepository
                        .countByStatus(SubscriptionStatus.ACTIVE);

        // =========================
        // TEMPLATE
        // =========================
        long totalTemplates = templateRepository.count();
        long activeTemplates =
                templateRepository.countByIsActiveTrue();

        // =========================
        // PAYMENT 🔥
        // =========================
        long totalPayments =
                paymentRepository.count();

        long successPayments =
                paymentRepository.countByStatus(
                        PaymentStatus.SUCCESS
                );

        long totalRevenue =
                paymentRepository
                        .sumAmountByStatus(PaymentStatus.SUCCESS);

        long paidUsers =
                paymentRepository
                        .countDistinctUserIdByStatus(
                                PaymentStatus.SUCCESS
                        );

        // =========================
        // BUILD RESPONSE
        // =========================
        return AdminDashboardResponse.builder()
                // USER
                .totalUsers(totalUsers)
                .verifiedUsers(verifiedUsers)
                .lockedUsers(lockedUsers)

                // CV
                .totalCVs(totalCVs)
                .publicCVs(publicCVs)

                // SUBSCRIPTION
                .freeUsers(freeUsers)
                .proUsers(proUsers)
                .premiumUsers(premiumUsers)
                .totalSubscriptionsActive(totalSubscriptionsActive)

                // TEMPLATE
                .totalTemplates(totalTemplates)
                .activeTemplates(activeTemplates)

                // PAYMENT
                .totalPayments(totalPayments)
                .successPayments(successPayments)
                .totalRevenue(totalRevenue)
                .paidUsers(paidUsers)

                .build();
    }
}
