package com.example.SmartCV.modules.cv.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.cv.domain.PlanType;
import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.example.SmartCV.modules.subscription.domain.SubscriptionStatus;
import com.example.SmartCV.modules.subscription.domain.UserSubscription;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    // =========================
    // Helpers
    // =========================

    /**
     * Lấy plan user, mặc định FREE nếu chưa mua gói
     */
    private PlanType getUserPlan(Long userId) {
        return userSubscriptionRepository.findByUserId(userId)
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .map(UserSubscription::getPlan)
                .orElse(PlanType.FREE);
    }

    /**
     * Trả về list plan được phép theo plan hiện tại
     */
    private List<PlanType> getAllowedPlans(PlanType userPlan) {
        List<PlanType> plans = new ArrayList<>();

        switch (userPlan) {
            case PREMIUM -> {
                plans.add(PlanType.FREE);
                plans.add(PlanType.PRO);
                plans.add(PlanType.PREMIUM);
            }
            case PRO -> {
                plans.add(PlanType.FREE);
                plans.add(PlanType.PRO);
            }
            case FREE -> {
                plans.add(PlanType.FREE);
            }
            default -> plans.add(PlanType.FREE);
        }

        return plans;
    }

    // =========================
    // Public APIs
    // =========================

    /**
     * Lấy danh sách template user được phép thấy
     */
    public List<Template> getAvailableTemplates(Long userId) {

        PlanType userPlan = getUserPlan(userId);
        List<PlanType> allowedPlans = getAllowedPlans(userPlan);

        return templateRepository.findByIsActiveTrueAndPlanRequiredIn(allowedPlans);
    }

    /**
     * Lấy chi tiết 1 template (khi user click xem)
     */
    public Template getTemplateDetail(Long userId, Long templateId) {

        PlanType userPlan = getUserPlan(userId);
        List<PlanType> allowedPlans = getAllowedPlans(userPlan);

        Template template = templateRepository.findById(templateId)
                .filter(Template::getIsActive)
                .orElseThrow(() -> new RuntimeException("Template không tồn tại hoặc đã bị vô hiệu hóa"));

        if (!allowedPlans.contains(template.getPlanRequired())) {
            throw new RuntimeException("Gói hiện tại không được phép sử dụng template này");
        }

        return template;
    }
}
