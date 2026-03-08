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
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.example.SmartCV.modules.cv.repository.CVRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.multipart.MultipartFile;

import com.example.SmartCV.common.service.PreviewStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminTemplateService {

    private final TemplateRepository templateRepository;
    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private final PreviewStorageService previewStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Removed HTML_POLICY to allow raw templates with Handlebars syntax

    // =========================
    // CREATE TEMPLATE
    // =========================
    @CacheEvict(value = "templates", allEntries = true)
    public Template createTemplate(
            String name,
            String previewContent,
            String fullContent,
            String configJson,
            PlanType planRequired) {
        String safePreview = sanitizeContent(previewContent);
        String safeFull = sanitizeContent(fullContent);

        Template template = Template.builder()
                .code(generateTemplateCode(name))
                .name(name)
                .previewContent(safePreview)
                .fullContent(safeFull)
                .configJson(configJson)
                .planRequired(planRequired)
                .isActive(true)
                .build();

        return templateRepository.save(template);
    }

    private String generateTemplateCode(String name) {
        return "TPL_" + name
                .toUpperCase()
                .replaceAll("\\s+", "_")
                + "_" + System.currentTimeMillis();
    }

    // =========================
    // UPDATE TEMPLATE
    // =========================
    @CacheEvict(value = "templates", allEntries = true)
    public Template updateTemplate(
            Long templateId,
            String name,
            String previewContent,
            String fullContent,
            String configJson,
            PlanType planRequired) {
        Template template = getTemplateOrThrow(templateId);

        String safePreview = sanitizeContent(previewContent);
        String safeFull = sanitizeContent(fullContent);

        template.setName(name);
        template.setPreviewContent(safePreview);
        template.setFullContent(safeFull);
        template.setConfigJson(configJson);
        template.setPlanRequired(planRequired);

        return templateRepository.save(template);
    }

    // =========================
    // UPLOAD THUMBNAIL
    // =========================
    @CacheEvict(value = "templates", allEntries = true)
    public String uploadThumbnail(Long templateId, MultipartFile file) {
        Template template = getTemplateOrThrow(templateId);
        String oldUri = template.getThumbnailUrl();

        String relativeUri = previewStorageService.save(file);
        template.setThumbnailUrl(relativeUri);
        templateRepository.save(template);

        if (oldUri != null && oldUri.startsWith("preview/")) {
            try {
                previewStorageService.delete(oldUri.replace("preview/", ""));
            } catch (Exception e) {
                // Log exception in a real system
            }
        }

        return relativeUri;
    }

    // =========================
    // DISABLE TEMPLATE (LOCK)
    // =========================
    @CacheEvict(value = "templates", allEntries = true)
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
    @CacheEvict(value = "templates", allEntries = true)
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
    @CacheEvict(value = "templates", allEntries = true)
    public void deleteTemplate(Long templateId) {

        Template template = getTemplateOrThrow(templateId);
        String oldUri = template.getThumbnailUrl();

        // 🔥 BẮT BUỘC: xử lý CV trước
        handleCVWhenTemplateDeleted(template);

        templateRepository.delete(template);

        if (oldUri != null && oldUri.startsWith("preview/")) {
            try {
                previewStorageService.delete(oldUri.replace("preview/", ""));
            } catch (Exception e) {
                // Log exception in a real system
            }
        }
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

    private void validateThumbnailUrl(String url) {
        if (url == null || url.trim().isEmpty())
            return;
        String lower = url.toLowerCase();
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("file:")) {
            throw new RuntimeException("Dangerous protocols are not allowed in Thumbnail URL");
        }
        if (!lower.startsWith("https://") && !lower.startsWith("http://localhost") && !lower.startsWith("/uploads/")) {
            throw new RuntimeException("Thumbnail URL must be a valid HTTPS URL or an uploaded resource.");
        }
    }

    private String sanitizeContent(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        try {
            JsonNode node = objectMapper.readTree(content);
            if (node.has("html") && node.isObject()) {
                String html = node.get("html").asText();
                String sanitizedHtml = html
                        .replaceAll("(?i)<script.*?>.*?</script>", "")
                        .replaceAll("(?i)<iframe.*?>.*?</iframe>", "")
                        .replaceAll("(?i)javascript:", "blocked:")
                        .replaceAll("(?i)onload\\s*=", "data-blocked=")
                        .replaceAll("(?i)onerror\\s*=", "data-blocked=");

                validateTemplatePlaceholders(sanitizedHtml);

                ((ObjectNode) node).put("html", sanitizedHtml);
            }
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            return content;
        }
    }

    private void validateTemplatePlaceholders(String html) {
        if (html == null)
            return;

        java.util.regex.Pattern p = java.util.regex.Pattern
                .compile("\\{\\{[&#^/]*\\s*(?:each\\s+|limitWords\\s+)?([a-zA-Z0-9.]+)(?:\\s+\\d+)?\\s*\\}\\}");
        java.util.regex.Matcher m = p.matcher(html);
        List<String> validKeys = List.of(
                "profile.name", "profile.title", "profile.summary", "profile.photo",
                "profile.email", "profile.phone", "profile.gender", "profile.dob",
                "profile.address", "profile.website",
                "careerObjective", "interests",
                "experience", "company", "position", "description", "startDate", "endDate",
                "education", "school", "degree", "major",
                "skills", "name", "level", "languages", "language", "proficiency",
                "references", "contact",
                "projects", "role", "link",
                "certifications", "issuer", "date",
                "awards", "year");

        while (m.find()) {
            String key = m.group(1);
            if (key.equals("else") || key.endsWith(".length") || key.equals("this"))
                continue;
            if (key.equals("experience") || key.equals("education") || key.equals("skills") || key.equals("languages")
                    || key.equals("references") || key.equals("projects") || key.equals("certifications")
                    || key.equals("awards"))
                continue;

            if (!validKeys.contains(key)) {
                throw new RuntimeException(
                        "Invalid template placeholder key: " + key + ". Please use only official schema keys.");
            }
        }
    }

    /**
     * Khi template bị LOCK
     * → CV phải bị khóa
     * → isPublic = false
     * → gửi mail cho user
     */
    private void handleCVWhenTemplateLocked(Template template) {

        List<CV> cvs = cvRepository.findByTemplateId(template.getId());

        if (cvs.isEmpty())
            return; // không ảnh hưởng ai → không mail

        for (CV cv : cvs) {
            cv.setStatus(CVStatus.TEMPLATE_LOCKED);
            cv.setIsPublic(false);
            cvRepository.save(cv);

            notifyUserTemplateAffected(
                    cv.getUserId(),
                    template.getName(),
                    "Template bạn đang sử dụng đã bị khóa. Vui lòng chọn template khác để tiếp tục chỉnh sửa CV.");
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

        if (cvs.isEmpty())
            return;

        for (CV cv : cvs) {
            cv.setStatus(CVStatus.TEMPLATE_DELETED);
            cv.setIsPublic(false);
            cvRepository.save(cv);

            notifyUserTemplateAffected(
                    cv.getUserId(),
                    template.getName(),
                    "Template bạn đang sử dụng đã bị xóa. CV đã bị đóng băng, vui lòng tạo CV mới với template khác.");
        }
    }

    // =========================
    // SEND MAIL
    // =========================
    private void notifyUserTemplateAffected(Long userId, String templateName, String reason) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return;

        emailService.sendTemplateAffectedEmail(
                user.getEmail(),
                templateName,
                reason);
    }

    // =========================
    // GET TEMPLATE
    // =========================
    private Template getTemplateOrThrow(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with id = " + templateId));
    }
}
