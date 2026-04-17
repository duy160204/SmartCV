package com.example.SmartCV.modules.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.admin.domain.AdminSubscriptionRequest;
import com.example.SmartCV.modules.admin.domain.AdminSubscriptionRequestStatus;
import com.example.SmartCV.modules.admin.service.AdminSubscriptionRequestService;
import com.example.SmartCV.modules.admin.service.AdminSubscriptionService;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewRequest;
import com.example.SmartCV.modules.admin.dto.SubscriptionPreviewResponse;
import com.example.SmartCV.modules.admin.dto.SubscriptionConfirmRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/subscription-requests")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionRequestController {

        private final AdminSubscriptionRequestService requestService;
        private final AdminSubscriptionService subscriptionService;

        // ==================================================
        // LIST ALL REQUESTS
        // ==================================================
        @GetMapping
        public ResponseEntity<List<AdminSubscriptionRequest>> listAll() {
                return ResponseEntity.ok(
                                requestService.findAll());
        }

        // ==================================================
        // LIST BY STATUS
        // ==================================================
        @GetMapping("/status/{status}")
        public ResponseEntity<List<AdminSubscriptionRequest>> listByStatus(
                        @PathVariable AdminSubscriptionRequestStatus status) {
                return ResponseEntity.ok(
                                requestService.findByStatus(status));
        }

        // ==================================================
        // PREVIEW (ADMIN)
        // ==================================================
        @PostMapping("/{id}/preview")
        @Transactional(readOnly = true)
        public ResponseEntity<SubscriptionPreviewResponse> preview(
                        @AuthenticationPrincipal UserPrincipal admin,
                        @PathVariable("id") Long requestId) {
                // FIXED: Pure Read - No Database Update here
                AdminSubscriptionRequest req = requestService.getOrThrow(requestId);

                SubscriptionPreviewRequest previewRequest = new SubscriptionPreviewRequest();
                previewRequest.setUserId(req.getUserId());
                previewRequest.setNewPlan(req.getRequestedPlan());
                previewRequest.setDurationMonths(req.getMonths()); // FIXED: Missing mapping

                return ResponseEntity.ok(
                                subscriptionService.preview(previewRequest));
        }

        // Optional: Manual mark as previewed if needed by UI
        @PostMapping("/{id}/mark-previewed")
        public ResponseEntity<?> markPreviewed(
                        @AuthenticationPrincipal UserPrincipal admin,
                        @PathVariable("id") Long requestId) {
                requestService.markPreviewed(requestId, admin.getId());
                return ResponseEntity.ok("Request marked as previewed");
        }

        // ==================================================
        // CONFIRM (ADMIN)
        // ==================================================
        @PostMapping("/{id}/confirm")
        @Transactional // FIXED: Atomic Transaction
        public ResponseEntity<?> confirm(
                        @AuthenticationPrincipal UserPrincipal admin,
                        @PathVariable("id") Long requestId) {
                
                // 1. Mark request as confirmed (updates request table)
                AdminSubscriptionRequest req = requestService.markConfirmed(requestId, admin.getId());

                // 2. Activate subscription (updates subscription and history tables)
                SubscriptionConfirmRequest confirmRequest = new SubscriptionConfirmRequest();
                confirmRequest.setUserId(req.getUserId());
                confirmRequest.setNewPlan(req.getRequestedPlan());
                confirmRequest.setDurationMonths(req.getMonths());
                confirmRequest.setConfirm(true);
                confirmRequest.setPaymentId(req.getPaymentId());

                subscriptionService.confirm(
                                admin.getId(),
                                confirmRequest);

                return ResponseEntity.ok("Subscription activated successfully");
        }
}
