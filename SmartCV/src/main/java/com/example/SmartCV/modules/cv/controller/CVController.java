package com.example.SmartCV.modules.cv.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.domain.CVFavorite;
import com.example.SmartCV.modules.cv.domain.CVShare;
import com.example.SmartCV.modules.cv.service.CVService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CVController {

    private final CVService cvService;

    // =========================
    // Helpers
    // =========================

    private Long getUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    // =========================
    // UC-B01 – Create CV
    // =========================
    @PostMapping
    public ResponseEntity<CV> createCV(
            @RequestBody CreateCVRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        CV cv = cvService.createCV(userId, request.getTemplateId(), request.getTitle(), request.getContent());
        return ResponseEntity.ok(cv);
    }

    // =========================
    // UC-B02 – Update CV
    // =========================
    @PutMapping("/{cvId}")
    public ResponseEntity<CV> updateCV(
            @PathVariable Long cvId,
            @RequestBody UpdateCVRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        CV cv = cvService.updateCV(userId, cvId, request.getTitle(), request.getContent());
        return ResponseEntity.ok(cv);
    }

    // =========================
    // UC-B03 – Auto Save
    // =========================
    @PatchMapping("/{cvId}/auto-save")
    public ResponseEntity<?> autoSave(
            @PathVariable Long cvId,
            @RequestBody AutoSaveRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        cvService.autoSave(userId, cvId, request.getContent());
        return ResponseEntity.ok().build();
    }

    // =========================
    // UC-B04 – Publish CV
    // =========================
    @PostMapping("/{cvId}/publish")
    public ResponseEntity<CV> publishCV(
            @PathVariable Long cvId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        CV cv = cvService.publishCV(userId, cvId);
        return ResponseEntity.ok(cv);
    }

    // =========================
    // UC-B05 – Share CV
    // =========================
    @PostMapping("/{cvId}/share")
    public ResponseEntity<CVShare> shareCV(
            @PathVariable Long cvId,
            @RequestParam(defaultValue = "7") int expireDays,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        CVShare share = cvService.shareCV(userId, cvId, expireDays);
        return ResponseEntity.ok(share);
    }

    @DeleteMapping("/{cvId}/share")
    public ResponseEntity<?> revokeShare(
            @PathVariable Long cvId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        cvService.revokeShare(userId, cvId);
        return ResponseEntity.ok().build();
    }

    // =========================
    // UC-B06 – Download CV
    // =========================
    @GetMapping("/{cvId}/download")
    public ResponseEntity<CV> downloadCV(
            @PathVariable Long cvId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        CV cv = cvService.getCVForDownload(userId, cvId);
        return ResponseEntity.ok(cv);
    }

    // =========================
    // UC-B07 – Delete CV
    // =========================
    @DeleteMapping("/{cvId}")
    public ResponseEntity<?> deleteCV(
            @PathVariable Long cvId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        cvService.deleteCV(userId, cvId);
        return ResponseEntity.ok().build();
    }

    // =========================
    // UC-B08 – Favorite Template
    // =========================
    @PostMapping("/favorite/{templateId}")
    public ResponseEntity<?> favoriteTemplate(
            @PathVariable Long templateId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        cvService.favoriteTemplate(userId, templateId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorite/{templateId}")
    public ResponseEntity<?> unfavoriteTemplate(
            @PathVariable Long templateId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        cvService.unfavoriteTemplate(userId, templateId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorite")
    public ResponseEntity<List<CVFavorite>> getFavorites(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(cvService.getFavorites(userId));
    }

    // =========================
    // Extra – List CV
    // =========================
    @GetMapping
    public ResponseEntity<List<CV>> getMyCVs(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(cvService.getMyCVs(userId));
    }

    @GetMapping("/{cvId}")
    public ResponseEntity<CV> getMyCVDetail(
            @PathVariable Long cvId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(cvService.getMyCVDetail(userId, cvId));
    }

    // =========================
    // Request DTO
    // =========================

    @Data
    public static class CreateCVRequest {
        private Long templateId;
        private String title;
        private String content;
    }

    @Data
    public static class UpdateCVRequest {
        private String title;
        private String content;
    }

    @Data
    public static class AutoSaveRequest {
        private String content;
    }
}
