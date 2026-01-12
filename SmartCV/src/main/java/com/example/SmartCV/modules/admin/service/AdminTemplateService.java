package com.example.SmartCV.modules.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.service.EmailService;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.domain.CVStatus;
import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.cv.repository.CVRepository;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminTemplateService {

    private final TemplateRepository templateRepository;
    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // =========================
    // CREATE TEMPLATE
    // =========================
    public Template createTemplate(
            String name,
            String thumbnailUrl,
            String previewContent,
            String fullContent,
            PlanType planRequired
    ) {
        Template template = Template.builder()
                .name(name)
                .thumbnailUrl(thumbnailUrl)
                .previewContent(previewContent)
                .fullContent(fullContent)
                .planRequired(planRequired)
                .isActive(true)
                .build();

        return templateRepository.save(template);
    }

    // =========================
    // UPDATE TEMPLATE
    // =========================
    public Template updateTemplate(
            Long templateId,
            String name,
            String thumbnailUrl,
            String previewContent,
            String fullContent,
            PlanType planRequired
    ) {
        Template template = getTemplateOrThrow(templateId);

        template.setName(name);
        template.setThumbnailUrl(thumbnailUrl);
        template.setPreviewContent(previewContent);
        template.setFullContent(fullContent);
        template.setPlanRequired(planRequired);

        return templateRepository.save(template);
    }

    // =========================
    // DISABLE TEMPLATE (LOCK)
    // =========================
    public void disableTemplate(Long templateId) {

        Template template = getTemplateOrThrow(templateId);

        if (Boolean.FALSE.equals(template.getIsActive())) {
            throw new RuntimeException("Template is already disabled");
        }

        template.setIsActive(false);
        templateRepository.save(template);

        // 🔥 BẮT BUỘC: xử lý CV trước, rồi mới mail
        handleCVWhenTemplateLocked(template);
    }

    // =========================
    // ENABLE TEMPLATE
    // =========================
    public void enableTemplate(Long templateId) {

        Template template = getTemplateOrThrow(templateId);

        if (Boolean.TRUE.equals(template.getIsActive())) {
            throw new RuntimeException("Template is already enabled");
        }

        template.setIsActive(true);
        templateRepository.save(template);

        // ❗ Enable không ảnh hưởng CV → không mail
    }

    // =========================
    // DELETE TEMPLATE
    // =========================
    public void deleteTemplate(Long templateId) {

        Template template = getTemplateOrThrow(templateId);

        // 🔥 BẮT BUỘC: xử lý CV trước
        handleCVWhenTemplateDeleted(template);

        templateRepository.delete(template);
    }

    // =========================
    // GET ALL (ADMIN)
    // =========================
    @Transactional(readOnly = true)
    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    // =========================
    // GET DETAIL
    // =========================
    @Transactional(readOnly = true)
    public Template getTemplateDetail(Long templateId) {
        return getTemplateOrThrow(templateId);
    }

    // ======================================================
    // =============== PRIVATE – CORE LOGIC =================
    // ======================================================

    /**
     * Khi template bị LOCK
     * → CV phải bị khóa
     * → isPublic = false
     * → gửi mail cho user
     */
    private void handleCVWhenTemplateLocked(Template template) {

        List<CV> cvs = cvRepository.findByTemplateId(template.getId());

        if (cvs.isEmpty()) return; // không ảnh hưởng ai → không mail

        for (CV cv : cvs) {
            cv.setStatus(CVStatus.TEMPLATE_LOCKED);
            cv.setIsPublic(false);
            cvRepository.save(cv);

            notifyUserTemplateAffected(
                    cv.getUserId(),
                    template.getName(),
                    "Template bạn đang sử dụng đã bị khóa. Vui lòng chọn template khác để tiếp tục chỉnh sửa CV."
            );
        }
    }

    /**
     * Khi template bị DELETE
     * → CV bị đóng băng
     * → isPublic = false
     * → gửi mail cho user
     */
    private void handleCVWhenTemplateDeleted(Template template) {

        List<CV> cvs = cvRepository.findByTemplateId(template.getId());

        if (cvs.isEmpty()) return;

        for (CV cv : cvs) {
            cv.setStatus(CVStatus.TEMPLATE_DELETED);
            cv.setIsPublic(false);
            cvRepository.save(cv);

            notifyUserTemplateAffected(
                    cv.getUserId(),
                    template.getName(),
                    "Template bạn đang sử dụng đã bị xóa. CV đã bị đóng băng, vui lòng tạo CV mới với template khác."
            );
        }
    }

    // =========================
    // SEND MAIL
    // =========================
    private void notifyUserTemplateAffected(Long userId, String templateName, String reason) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        emailService.sendTemplateAffectedEmail(
                user.getEmail(),
                templateName,
                reason
        );
    }

    // =========================
    // GET TEMPLATE
    // =========================
    private Template getTemplateOrThrow(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with id = " + templateId));
    }
}
