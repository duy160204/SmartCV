package com.example.SmartCV.modules.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.admin.dto.AdminCVDetailResponse;
import com.example.SmartCV.modules.admin.dto.AdminCVListResponse;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.service.EmailService;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.repository.CVRepository;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.example.SmartCV.modules.cv.domain.Template;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCVService {

    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    // =========================
    // LIST ALL CV
    // =========================
    @Transactional(readOnly = true)
    public List<AdminCVListResponse> getAllCVs() {
        return cvRepository.findAll()
                .stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // CV DETAIL
    // =========================
    @Transactional(readOnly = true)
    public AdminCVDetailResponse getCVDetail(Long cvId) {

        CV cv = getCVOrThrow(cvId);

        return AdminCVDetailResponse.builder()
                .id(cv.getId())
                .userId(cv.getUserId())
                .title(cv.getTitle())
                .templateId(cv.getTemplateId())
                .content(cv.getContent())
                .status(cv.getStatus())
                .isPublic(cv.getIsPublic())
                .isLocked(cv.getIsLocked())
                .viewCount(cv.getViewCount())
                .createdAt(cv.getCreatedAt())
                .updatedAt(cv.getUpdatedAt())
                .templateHtml(extractHtml(cv.getTemplateId()))
                .templateCss(extractCss(cv.getTemplateId()))
                .build();
    }

    // =========================
    // LOCK CV
    // =========================
    public void lockCV(Long cvId, String reason) {

        CV cv = getCVOrThrow(cvId);

        if (Boolean.TRUE.equals(cv.getIsLocked())) {
            throw new RuntimeException("CV already locked");
        }

        cv.setIsLocked(true);
        cvRepository.save(cv);

        notifyUser(cv, "LOCK", reason);
    }

    // =========================
    // UNLOCK CV
    // =========================
    public void unlockCV(Long cvId, String reason) {

        CV cv = getCVOrThrow(cvId);

        if (!Boolean.TRUE.equals(cv.getIsLocked())) {
            throw new RuntimeException("CV is not locked");
        }

        cv.setIsLocked(false);
        cvRepository.save(cv);

        notifyUser(cv, "UNLOCK", reason);
    }

    // =========================
    // DELETE CV
    // =========================
    public void deleteCV(Long cvId, String reason) {

        CV cv = getCVOrThrow(cvId);

        notifyUser(cv, "DELETE", reason);

        cvRepository.delete(cv);
    }

    // =========================
    // PRIVATE HELPERS
    // =========================
    private CV getCVOrThrow(Long cvId) {
        return cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV not found with id = " + cvId));
    }

    private AdminCVListResponse toListResponse(CV cv) {
        String ownerEmail = "Unknown";
        if (cv.getUserId() != null) {
            ownerEmail = userRepository.findById(cv.getUserId())
                    .map(User::getEmail)
                    .orElse("Unknown");
        }

        return AdminCVListResponse.builder()
                .id(cv.getId())
                .userId(cv.getUserId())
                .ownerEmail(ownerEmail)
                .title(cv.getTitle())
                .templateId(cv.getTemplateId())
                .isPublic(cv.getIsPublic())
                .isLocked(cv.getIsLocked())
                .createdAt(cv.getCreatedAt())
                .build();
    }

    // Helper to extract template HTML/CSS safely
    private String extractHtml(Long templateId) {
        if (templateId == null)
            return "";
        Template tmpl = templateRepository.findById(templateId).orElse(null);
        if (tmpl == null || tmpl.getFullContent() == null)
            return "";

        try {
            JsonNode node = objectMapper.readTree(tmpl.getFullContent());
            if (node.has("html"))
                return node.get("html").asText();
        } catch (Exception e) {
            // Not JSON, return full content if it looks like HTML
            if (tmpl.getFullContent().trim().startsWith("<"))
                return tmpl.getFullContent();
        }
        return "";
    }

    private String extractCss(Long templateId) {
        if (templateId == null)
            return "";
        Template tmpl = templateRepository.findById(templateId).orElse(null);
        if (tmpl == null || tmpl.getFullContent() == null)
            return "";

        try {
            JsonNode node = objectMapper.readTree(tmpl.getFullContent());
            if (node.has("css"))
                return node.get("css").asText();
        } catch (Exception e) {
            // Ignore format error
        }
        return "";
    }

    private void notifyUser(CV cv, String action, String reason) {

        User user = userRepository.findById(cv.getUserId()).orElse(null);
        if (user == null)
            return;

        emailService.sendCVAffectedEmail(
                user.getEmail(),
                cv.getTitle(),
                action,
                reason);
    }
}
