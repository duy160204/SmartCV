package com.example.SmartCV.modules.subscription.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.subscription.dto.PublicLinkResponseDTO;
import com.example.SmartCV.modules.subscription.dto.SimpleResponseDTO;
import com.example.SmartCV.modules.subscription.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // =========================
    // PUBLIC CV (SHARE)
    // =========================
    @PostMapping("/cv/{cvId}/public")
    public ResponseEntity<SimpleResponseDTO> publicCV(
            @PathVariable Long cvId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.getId();
        PublicLinkResponseDTO dto = subscriptionService.publicCV(userId, cvId);
        return ResponseEntity.ok(
                new SimpleResponseDTO("Public link created", dto)
        );
    }

    // =========================
    // REVOKE PUBLIC LINK
    // =========================
    @DeleteMapping("/cv/{cvId}/public")
    public ResponseEntity<SimpleResponseDTO> revokePublicLink(
            @PathVariable Long cvId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.getId();
        subscriptionService.revokePublicLink(userId, cvId);
        return ResponseEntity.ok(
                new SimpleResponseDTO("Public link revoked")
        );
    }

    // =========================
    // CHECK DOWNLOAD PERMISSION
    // =========================
    @GetMapping("/download/check")
    public ResponseEntity<SimpleResponseDTO> checkDownloadPermission(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.getId();
        subscriptionService.checkDownloadPermission(userId);
        return ResponseEntity.ok(
                new SimpleResponseDTO("Download allowed")
        );
    }

    // =========================
    // GET MY SUBSCRIPTION INFO
    // =========================
    @GetMapping("/me")
    public ResponseEntity<SimpleResponseDTO> getMySubscription(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.getId();
        return ResponseEntity.ok(
                new SimpleResponseDTO(
                        "Subscription info",
                        subscriptionService.getMySubscriptionInfo(userId)
                )
        );
    }
}
