package com.example.SmartCV.modules.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.admin.dto.SubscriptionConfirmRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewResponse;
import com.example.SmartCV.modules.admin.service.AdminSubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

    private final AdminSubscriptionService adminSubscriptionService;

    // =========================
    // PREVIEW – chỉ tính, không update
    // =========================
    @PostMapping("/preview")
    public ResponseEntity<SubscriptionPreviewResponse> preview(
            @RequestBody SubscriptionPreviewRequest request
    ) {
        SubscriptionPreviewResponse response = adminSubscriptionService.preview(request);
        return ResponseEntity.ok(response);
    }

    // =========================
    // CONFIRM – update thật
    // =========================
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(
            @RequestBody SubscriptionConfirmRequest request
    ) {
        adminSubscriptionService.confirm(request);
        return ResponseEntity.ok().build();
    }
}
