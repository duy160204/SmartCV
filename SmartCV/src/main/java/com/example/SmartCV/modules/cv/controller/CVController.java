package com.example.SmartCV.modules.cv.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.domain.CVFavorite;
import com.example.SmartCV.modules.cv.dto.CVDTO;
import com.example.SmartCV.modules.cv.dto.projection.CVListProjection;
import com.example.SmartCV.modules.cv.service.CVService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CVController {

        private final CVService cvService;

        // =========================
        // UC-B01 – Create CV
        // =========================
        @PostMapping
        public ResponseEntity<CV> createCV(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @RequestBody @Valid CVDTO body) {
                CV cv = cvService.createCV(principal.getId(), body.getTemplateId(), body.getTitle(), body.getContent());
                return ResponseEntity.ok(cv);
        }

        // =========================
        // UC-B02 – Update CV
        // =========================
        @PutMapping("/{cvId}")
        public ResponseEntity<CV> updateCV(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long cvId,
                        @RequestBody @Valid CVDTO body) {
                // Validation handled by @Valid
                CV cv = cvService.updateCV(principal.getId(), cvId, body.getTitle(), body.getContent());
                return ResponseEntity.ok(cv);
        }

        // =========================
        // UC-B03 – Auto Save
        // =========================
        @PatchMapping("/{cvId}/autosave")
        public ResponseEntity<Void> autoSave(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long cvId,
                        @RequestBody Map<String, String> body // Keep Map for partial update or simple structure
        ) {
                String content = body.get("content");
                if (content == null) {
                        return ResponseEntity.badRequest().build();
                }
                cvService.autoSave(principal.getId(), cvId, content);
                return ResponseEntity.ok().build();
        }

        // =========================
        // UC-B04 – Publish CV
        // =========================
        @PostMapping("/{cvId}/publish")
        public ResponseEntity<CV> publishCV(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long cvId) {
                Long userId = principal.getId();
                CV cv = cvService.publishCV(userId, cvId);
                return ResponseEntity.ok(cv);
        }

        // =========================
        // UC-B05 – Download CV (PDF)
        // =========================
        @GetMapping("/{cvId}/download")
        public ResponseEntity<byte[]> downloadCV(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long cvId) {
                Long userId = principal.getId();

                byte[] pdfBytes = cvService.downloadCV(userId, cvId);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=cv-" + cvId + ".pdf")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(pdfBytes);
        }

        // =========================
        // UC-B06 – Delete CV
        // =========================
        @DeleteMapping("/{cvId}")
        public ResponseEntity<Void> deleteCV(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long cvId) {
                cvService.deleteCV(principal.getId(), cvId);
                return ResponseEntity.ok().build();
        }

        // =========================
        // UC-B07 – Favorite Template
        // =========================
        @PostMapping("/template/{templateId}/favorite")
        public ResponseEntity<Void> favoriteTemplate(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long templateId) {
                cvService.favoriteTemplate(principal.getId(), templateId);
                return ResponseEntity.ok().build();
        }

        @DeleteMapping("/template/{templateId}/favorite")
        public ResponseEntity<Void> unfavoriteTemplate(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long templateId) {
                cvService.unfavoriteTemplate(principal.getId(), templateId);
                return ResponseEntity.ok().build();
        }

        @GetMapping("/favorites")
        public ResponseEntity<List<CVFavorite>> getFavorites(
                        @AuthenticationPrincipal UserPrincipal principal) {
                return ResponseEntity.ok(
                                cvService.getFavorites(principal.getId()));
        }

        // =========================
        // UC-B08 – List CV (Optimized)
        // =========================
        @GetMapping
        public ResponseEntity<List<CVListProjection>> getMyCVs(
                        @AuthenticationPrincipal UserPrincipal principal) {
                return ResponseEntity.ok(
                                cvService.getMyCVProjections(principal.getId()));
        }

        @GetMapping("/{cvId}")
        public ResponseEntity<CV> getMyCVDetail(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long cvId) {
                return ResponseEntity.ok(
                                cvService.getMyCVDetail(principal.getId(), cvId));
        }
}
