package com.example.SmartCV.modules.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.admin.dto.AdminCVDetailResponse;
import com.example.SmartCV.modules.admin.dto.AdminCVListResponse;
import com.example.SmartCV.modules.admin.service.AdminCVService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/cv")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminCVController {

    private final AdminCVService adminCVService;

    // =========================
    // GET ALL CV
    // =========================
    @GetMapping
    public ResponseEntity<List<AdminCVListResponse>> getAllCVs() {
        return ResponseEntity.ok(adminCVService.getAllCVs());
    }

    // =========================
    // GET CV DETAIL
    // =========================
    @GetMapping("/{cvId}")
    public ResponseEntity<AdminCVDetailResponse> getCVDetail(@PathVariable Long cvId) {
        return ResponseEntity.ok(adminCVService.getCVDetail(cvId));
    }

    // =========================
    // LOCK CV
    // =========================
    @PostMapping("/{cvId}/lock")
    public ResponseEntity<?> lockCV(
            @PathVariable Long cvId,
            @RequestParam(required = false) String reason) {
        adminCVService.lockCV(cvId, reason != null ? reason : "Admin locked this CV");
        return ResponseEntity.ok("CV locked successfully");
    }

    // =========================
    // UNLOCK CV
    // =========================
    @PostMapping("/{cvId}/unlock")
    public ResponseEntity<?> unlockCV(
            @PathVariable Long cvId,
            @RequestParam(required = false) String reason) {
        adminCVService.unlockCV(cvId, reason != null ? reason : "Admin unlocked this CV");
        return ResponseEntity.ok("CV unlocked successfully");
    }

    // =========================
    // DELETE CV
    // =========================
    @DeleteMapping("/{cvId}")
    public ResponseEntity<?> deleteCV(
            @PathVariable Long cvId,
            @RequestParam(required = false) String reason) {
        adminCVService.deleteCV(cvId, reason != null ? reason : "Admin deleted this CV");
        return ResponseEntity.ok("CV deleted successfully");
    }
}
