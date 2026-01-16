package com.example.SmartCV.modules.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.dto.ApiResponse;
import com.example.SmartCV.modules.admin.dto.AdminCVDetailResponse;
import com.example.SmartCV.modules.admin.dto.AdminCVListResponse;
import com.example.SmartCV.modules.admin.dto.CVActionRequestDTO;
import com.example.SmartCV.modules.admin.service.AdminCVService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/cv")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminCVController {

    private final AdminCVService adminCVService;

    // =========================
    // GET ALL CV
    // =========================
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminCVListResponse>>> getAllCVs() {
        return ResponseEntity.ok(ApiResponse.success(adminCVService.getAllCVs()));
    }

    // =========================
    // GET CV DETAIL
    // =========================
    @GetMapping("/{cvId}")
    public ResponseEntity<ApiResponse<AdminCVDetailResponse>> getCVDetail(@PathVariable Long cvId) {
        return ResponseEntity.ok(ApiResponse.success(adminCVService.getCVDetail(cvId)));
    }

    // =========================
    // LOCK CV
    // =========================
    @PostMapping("/{cvId}/lock")
    public ResponseEntity<ApiResponse<Void>> lockCV(
            @PathVariable Long cvId,
            @RequestBody CVActionRequestDTO request) {
        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "Admin locked this CV";
        log.info("Request to LOCK CV: {} | Reason: {}", cvId, reason);
        adminCVService.lockCV(cvId, reason);
        return ResponseEntity.ok(ApiResponse.success("CV locked successfully"));
    }

    // =========================
    // UNLOCK CV
    // =========================
    @PostMapping("/{cvId}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockCV(
            @PathVariable Long cvId,
            @RequestBody CVActionRequestDTO request) {
        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "Admin unlocked this CV";
        log.info("Request to UNLOCK CV: {} | Reason: {}", cvId, reason);
        adminCVService.unlockCV(cvId, reason);
        return ResponseEntity.ok(ApiResponse.success("CV unlocked successfully"));
    }

    // =========================
    // DELETE CV
    // =========================
    @DeleteMapping("/{cvId}")
    public ResponseEntity<ApiResponse<Void>> deleteCV(
            @PathVariable Long cvId,
            @RequestBody CVActionRequestDTO request) {
        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "Admin deleted this CV";
        log.info("Request to DELETE CV: {} | Reason: {}", cvId, reason);
        adminCVService.deleteCV(cvId, reason);
        return ResponseEntity.ok(ApiResponse.success("CV deleted successfully"));
    }
}
