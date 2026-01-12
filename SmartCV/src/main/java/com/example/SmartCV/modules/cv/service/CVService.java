package com.example.SmartCV.modules.cv.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.cv.domain.*;
import com.example.SmartCV.modules.cv.repository.*;
import com.example.SmartCV.modules.subscription.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CVService {

    private final CVRepository cvRepository;
    private final TemplateRepository templateRepository;
    private final CVFavoriteRepository cvFavoriteRepository;

    private final SubscriptionService subscriptionService;
    private final CVExportService cvExportService;

    // =========================
    // Helpers
    // =========================

    private CV getOwnedCV(Long cvId, Long userId) {
        return cvRepository.findById(cvId)
                .filter(cv -> cv.getUserId().equals(userId))
                .orElseThrow(() ->
                        new RuntimeException("CV không tồn tại hoặc không thuộc quyền sở hữu"));
    }

    private void checkTemplateStillValid(CV cv) {
        if (cv.getStatus() == CVStatus.TEMPLATE_LOCKED) {
            throw new RuntimeException("Template của CV này đã bị khóa, không thể thao tác");
        }
        if (cv.getStatus() == CVStatus.TEMPLATE_DELETED) {
            throw new RuntimeException("Template của CV này đã bị xóa, không thể thao tác");
        }
    }

    // =========================
    // UC-B01 – Create CV
    // =========================

    public CV createCV(Long userId, Long templateId, String title, String content) {

        subscriptionService.checkCanCreateCV(userId);

        Template template = templateRepository.findById(templateId)
                .filter(Template::getIsActive)
                .orElseThrow(() ->
                        new RuntimeException("Template không tồn tại hoặc đã bị vô hiệu hóa"));

        CV cv = CV.builder()
                .userId(userId)
                .templateId(template.getId())
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

        checkTemplateStillValid(cv);

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

        checkTemplateStillValid(cv);

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

        checkTemplateStillValid(cv);

        if (cv.getStatus() == CVStatus.ARCHIVED) {
            throw new RuntimeException("CV đã archive, không thể publish");
        }

        cv.setStatus(CVStatus.PUBLISHED);
        cv.setIsPublic(true);

        return cvRepository.save(cv);
    }

    // =========================
    // UC-B05 – Download CV (PDF)
    // =========================

    public byte[] downloadCV(Long userId, Long cvId) {

        subscriptionService.checkDownloadPermission(userId);

        CV cv = getOwnedCV(cvId, userId);

        checkTemplateStillValid(cv);

        cv.setViewCount(cv.getViewCount() + 1);
        cvRepository.save(cv);

        return cvExportService.exportToPdf(cv);
    }

    // =========================
    // UC-B06 – Delete CV
    // =========================

    public void deleteCV(Long userId, Long cvId) {

        CV cv = getOwnedCV(cvId, userId);
        cvRepository.delete(cv);
    }

    // =========================
    // UC-B07 – Favorite Template
    // =========================

    public void favoriteTemplate(Long userId, Long templateId) {

        if (cvFavoriteRepository.existsByUserIdAndTemplateId(userId, templateId)) {
            return;
        }

        Template template = templateRepository.findById(templateId)
                .filter(Template::getIsActive)
                .orElseThrow(() -> new RuntimeException("Template không tồn tại hoặc đã bị vô hiệu hóa"));

        CVFavorite favorite = CVFavorite.builder()
                .userId(userId)
                .templateId(template.getId())
                .build();

        cvFavoriteRepository.save(favorite);
    }

    public void unfavoriteTemplate(Long userId, Long templateId) {
        cvFavoriteRepository.deleteByUserIdAndTemplateId(userId, templateId);
    }

    public List<CVFavorite> getFavorites(Long userId) {
        return cvFavoriteRepository.findByUserId(userId);
    }

    // =========================
    // UC-B08 – List CV
    // =========================

    public List<CV> getMyCVs(Long userId) {
        return cvRepository.findByUserId(userId);
    }

    public CV getMyCVDetail(Long userId, Long cvId) {
        return getOwnedCV(cvId, userId);
    }
}
