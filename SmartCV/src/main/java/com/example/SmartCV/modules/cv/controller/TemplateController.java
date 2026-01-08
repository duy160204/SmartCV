package com.example.SmartCV.modules.cv.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.cv.service.TemplateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    // =========================
    // Helpers
    // =========================

    private Long getUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    // =========================
    // APIs
    // =========================

    /**
     * Lấy danh sách template user được phép thấy
     */
    @GetMapping
    public ResponseEntity<List<Template>> getTemplates(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(templateService.getAvailableTemplates(userId));
    }

    /**
     * Lấy chi tiết 1 template
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<Template> getTemplateDetail(
            @PathVariable Long templateId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(templateService.getTemplateDetail(userId, templateId));
    }
}
