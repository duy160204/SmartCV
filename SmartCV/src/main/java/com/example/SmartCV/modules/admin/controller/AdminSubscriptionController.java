package com.example.SmartCV.modules.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.admin.dto.SubscriptionConfirmRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewResponse;
import com.example.SmartCV.modules.admin.service.AdminSubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

        private final AdminSubscriptionService adminSubscriptionService;

        // =========================
        // PREVIEW – chỉ xem trước, không update DB
        // =========================
        @PostMapping("/preview")
        public ResponseEntity<SubscriptionPreviewResponse> preview(
                        @RequestBody SubscriptionPreviewRequest request) {
                return ResponseEntity.ok(
                                adminSubscriptionService.preview(request));
        }

        // =========================
        // CONFIRM – admin xác nhận update thật
        // =========================
        @PostMapping("/confirm")
        public ResponseEntity<?> confirm(
                        @AuthenticationPrincipal UserPrincipal admin,
                        @RequestBody SubscriptionConfirmRequest request) {
                adminSubscriptionService.confirm(
                                admin.getId(), // ✅ adminId lấy từ JWT
                                request);

                return ResponseEntity.ok("Subscription updated successfully");
        }
}
