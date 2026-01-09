package com.example.SmartCV.modules.cv.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.UserPrincipal;
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
    private Long getUserId(UserPrincipal principal) {
        return principal.getId();
    }

    // =========================
    // UC-B01 – Create CV
    // =========================
    @PostMapping
    public ResponseEntity<CV> createCV(
            @RequestBody CreateCVRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        CV cv = cvService.createCV(
                userId,
                request.getTemplateId(),
                request.getTitle(),
                request.getContent()
        );
        return ResponseEntity.ok(cv);
    }

    // =========================
    // UC-B02 – Update CV
    // =========================
    @PutMapping("/{cvId}")
    public ResponseEntity<CV> updateCV(
            @PathVariable Long cvId,
            @RequestBody UpdateCVRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        CV cv = cvService.updateCV(
                userId,
                cvId,
                request.getTitle(),
                request.getContent()
        );
        return ResponseEntity.ok(cv);
    }

    // =========================
    // UC-B03 – Auto Save
    // =========================
    @PatchMapping("/{cvId}/auto-save")
    public ResponseEntity<Void> autoSave(
            @PathVariable Long cvId,
            @RequestBody AutoSaveRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        cvService.autoSave(userId, cvId, request.getContent());
        return ResponseEntity.ok().build();
    }

    // =========================
    // UC-B04 – Publish CV
    // =========================
    @PostMapping("/{cvId}/publish")
    public ResponseEntity<CV> publishCV(
            @PathVariable Long cvId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
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
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        CVShare share = cvService.shareCV(userId, cvId, expireDays);
        return ResponseEntity.ok(share);
    }

    @DeleteMapping("/{cvId}/share")
    public ResponseEntity<Void> revokeShare(
            @PathVariable Long cvId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        cvService.revokeShare(userId, cvId);
        return ResponseEntity.ok().build();
    }

    // =========================
    // UC-B06 – Download CV
    // =========================
    @GetMapping("/{cvId}/download")
    public ResponseEntity<CV> downloadCV(
            @PathVariable Long cvId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        CV cv = cvService.getCVForDownload(userId, cvId);
        return ResponseEntity.ok(cv);
    }

    // =========================
    // UC-B07 – Delete CV
    // =========================
    @DeleteMapping("/{cvId}")
    public ResponseEntity<Void> deleteCV(
            @PathVariable Long cvId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        cvService.deleteCV(userId, cvId);
        return ResponseEntity.ok().build();
    }

    // =========================
    // UC-B08 – Favorite Template
    // =========================
    @PostMapping("/favorite/{templateId}")
    public ResponseEntity<Void> favoriteTemplate(
            @PathVariable Long templateId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        cvService.favoriteTemplate(userId, templateId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorite/{templateId}")
    public ResponseEntity<Void> unfavoriteTemplate(
            @PathVariable Long templateId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        cvService.unfavoriteTemplate(userId, templateId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorite")
    public ResponseEntity<List<CVFavorite>> getFavorites(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(cvService.getFavorites(userId));
    }

    // =========================
    // Extra – List CV
    // =========================
    @GetMapping
    public ResponseEntity<List<CV>> getMyCVs(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(cvService.getMyCVs(userId));
    }

    @GetMapping("/{cvId}")
    public ResponseEntity<CV> getMyCVDetail(
            @PathVariable Long cvId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = getUserId(principal);
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
