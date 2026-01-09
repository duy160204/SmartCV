package com.example.SmartCV.modules.cv.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.cv.domain.*;
import com.example.SmartCV.modules.cv.repository.*;
import com.example.SmartCV.modules.subscription.domain.SubscriptionStatus;
import com.example.SmartCV.modules.subscription.domain.UserSubscription;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CVService {

    private final CVRepository cvRepository;
    private final TemplateRepository templateRepository;
    private final CVShareRepository cvShareRepository;
    private final CVFavoriteRepository cvFavoriteRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    private static final int FREE_CV_LIMIT = 5;

    // =========================
    // Helpers
    // =========================

    private PlanType getUserPlan(Long userId) {
        return userSubscriptionRepository.findByUserId(userId)
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .map(UserSubscription::getPlan)
                .orElse(PlanType.FREE);
    }

    private CV getOwnedCV(Long cvId, Long userId) {
        return cvRepository.findById(cvId)
                .filter(cv -> cv.getUserId().equals(userId))
                .orElseThrow(() ->
                        new RuntimeException("CV không tồn tại hoặc không thuộc quyền sở hữu"));
    }

    private void checkCVQuota(Long userId, PlanType plan) {
        if (plan == PlanType.FREE) {
            long count = cvRepository.countByUserId(userId);
            if (count >= FREE_CV_LIMIT) {
                throw new RuntimeException(
                        "FREE plan chỉ được tạo tối đa " + FREE_CV_LIMIT + " CV");
            }
        }
    }

    private void checkTemplatePermission(Template template, PlanType userPlan) {
        if (!userPlan.isAtLeast(template.getPlanRequired())) {
            throw new RuntimeException("Gói hiện tại không cho phép sử dụng template này");
        }
    }

    // =========================
    // UC-B01 – Create CV
    // =========================

    public CV createCV(Long userId, Long templateId, String title, String content) {

        PlanType userPlan = getUserPlan(userId);
        checkCVQuota(userId, userPlan);

        Template template = templateRepository.findById(templateId)
                .filter(Template::getIsActive)
                .orElseThrow(() ->
                        new RuntimeException("Template không tồn tại hoặc đã bị vô hiệu hóa"));

        checkTemplatePermission(template, userPlan);

        CV cv = CV.builder()
                .userId(userId)
                .templateId(templateId)
                .title(title)
                .content(content)
                .status(CVStatus.DRAFT)
                .isPublic(false)
                .viewCount(0L)
                .build();

        return cvRepository.save(cv);
    }

    // =========================
    // UC-B02 – Update CV
    // =========================

    public CV updateCV(Long userId, Long cvId, String title, String content) {

        CV cv = getOwnedCV(cvId, userId);

        if (cv.getStatus() == CVStatus.ARCHIVED) {
            throw new RuntimeException("CV đã archive, không thể chỉnh sửa");
        }

        cv.setTitle(title);
        cv.setContent(content);

        return cvRepository.save(cv);
    }

    // =========================
    // UC-B03 – Auto Save
    // =========================

    public void autoSave(Long userId, Long cvId, String content) {

        CV cv = getOwnedCV(cvId, userId);

        if (cv.getStatus() == CVStatus.ARCHIVED) {
            throw new RuntimeException("CV đã archive, không thể auto-save");
        }

        cv.setContent(content);
        cvRepository.save(cv);
    }

    // =========================
    // UC-B04 – Publish CV
    // =========================

    public CV publishCV(Long userId, Long cvId) {

        CV cv = getOwnedCV(cvId, userId);

        if (cv.getStatus() == CVStatus.ARCHIVED) {
            throw new RuntimeException("CV đã archive, không thể publish");
        }

        cv.setStatus(CVStatus.PUBLISHED);
        cv.setIsPublic(true);

        return cvRepository.save(cv);
    }

    // =========================
    // UC-B05 – Share CV (FIX LOGIC)
    // =========================

    public CVShare shareCV(Long userId, Long cvId, int expireDays) {

        PlanType plan = getUserPlan(userId);
        if (plan == PlanType.FREE) {
           // throw new RuntimeException("FREE plan không được phép share CV");
        }

        CV cv = getOwnedCV(cvId, userId);

        if (cv.getStatus() != CVStatus.PUBLISHED) {
            throw new RuntimeException("Chỉ được share CV đã publish");
        }

        if (cvShareRepository.existsByCvId(cvId)) {
            throw new RuntimeException("CV này đã được share");
        }

        CVShare share = CVShare.builder()
                .cvId(cvId)
                .userId(userId)
                .shareUuid(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(expireDays))
                .build();

        // ❗ Không đổi status
        cv.setIsPublic(true);
        cvRepository.save(cv);

        return cvShareRepository.save(share);
    }

    public void revokeShare(Long userId, Long cvId) {

        CV cv = getOwnedCV(cvId, userId);

        if (!cvShareRepository.existsByCvId(cvId)) {
            throw new RuntimeException("CV này chưa được share");
        }

        cvShareRepository.deleteByCvId(cvId);

        // ❗ Thu hồi public, KHÔNG đổi status
        cv.setIsPublic(false);
        cvRepository.save(cv);
    }

    // =========================
    // UC-B06 – Download CV
    // =========================

   public CV getCVForDownload(Long userId, Long cvId) {
    PlanType plan = getUserPlan(userId);

    if (plan == PlanType.FREE) {
       // throw new RuntimeException("FREE plan không được download CV");
    }

    CV cv = getOwnedCV(cvId, userId);
    cv.setViewCount(cv.getViewCount() + 1);

    return cvRepository.save(cv);
    }

    // =========================
    // UC-B07 – Delete CV
    // =========================

    public void deleteCV(Long userId, Long cvId) {

        CV cv = getOwnedCV(cvId, userId);

        cvShareRepository.deleteByCvId(cvId);
        cvRepository.delete(cv);
    }

    // =========================
    // UC-B08 – Favorite Template
    // =========================

    public void favoriteTemplate(Long userId, Long templateId) {

        if (cvFavoriteRepository.existsByUserIdAndTemplateId(userId, templateId)) {
            return;
        }

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template không tồn tại"));

        CVFavorite fav = CVFavorite.builder()
                .userId(userId)
                .templateId(template.getId())
                .build();

        cvFavoriteRepository.save(fav);
    }

    public void unfavoriteTemplate(Long userId, Long templateId) {
        cvFavoriteRepository.deleteByUserIdAndTemplateId(userId, templateId);
    }

    public List<CVFavorite> getFavorites(Long userId) {
        return cvFavoriteRepository.findByUserId(userId);
    }

    // =========================
    // Extra – List CV
    // =========================

    public List<CV> getMyCVs(Long userId) {
        return cvRepository.findByUserId(userId);
    }

    public CV getMyCVDetail(Long userId, Long cvId) {
        return getOwnedCV(cvId, userId);
    }
}
