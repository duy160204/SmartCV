package com.example.SmartCV.modules.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class AdminSubscriptionRequestController {

    private final AdminSubscriptionRequestService requestService;
    private final AdminSubscriptionService subscriptionService;

    // ==================================================
    // LIST ALL REQUESTS
    // ==================================================
    @GetMapping
    public ResponseEntity<List<AdminSubscriptionRequest>> listAll() {
        return ResponseEntity.ok(
                requestService.findAll()
        );
    }

    // ==================================================
    // LIST BY STATUS
    // ==================================================
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AdminSubscriptionRequest>> listByStatus(
            @PathVariable AdminSubscriptionRequestStatus status
    ) {
        return ResponseEntity.ok(
                requestService.findByStatus(status)
        );
    }

    // ==================================================
    // PREVIEW (ADMIN)
    // ==================================================
    @PostMapping("/{id}/preview")
    public ResponseEntity<SubscriptionPreviewResponse> preview(
            @AuthenticationPrincipal UserPrincipal admin,
            @PathVariable("id") Long requestId
    ) {
        AdminSubscriptionRequest req =
                requestService.markPreviewed(requestId, admin.getId());

        // 🔥 build DTO ĐÚNG CÁCH (KHÔNG new constructor)
        SubscriptionPreviewRequest previewRequest =
                new SubscriptionPreviewRequest();
        previewRequest.setUserId(req.getUserId());
        previewRequest.setNewPlan(req.getRequestedPlan());

        return ResponseEntity.ok(
                subscriptionService.preview(previewRequest)
        );
    }

    // ==================================================
    // CONFIRM (ADMIN)
    // ==================================================
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(
            @AuthenticationPrincipal UserPrincipal admin,
            @PathVariable("id") Long requestId
    ) {
        AdminSubscriptionRequest req =
                requestService.markConfirmed(requestId, admin.getId());

        SubscriptionConfirmRequest confirmRequest =
                new SubscriptionConfirmRequest();
        confirmRequest.setUserId(req.getUserId());
        confirmRequest.setNewPlan(req.getRequestedPlan());
        confirmRequest.setConfirm(true);

        subscriptionService.confirm(
                admin.getId(),
                confirmRequest
        );

        return ResponseEntity.ok("Subscription activated successfully");
    }
}
