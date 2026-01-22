package com.example.SmartCV.modules.user.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartCV.common.exception.ResourceNotFoundException;
import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.user.dto.ChangePasswordRequest;
import com.example.SmartCV.modules.user.dto.UpdateProfileRequest;
import com.example.SmartCV.modules.user.dto.UserResponseDTO;
import com.example.SmartCV.modules.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Get current logged-in user (Session Rehydration)
     * IMPORTANT: no-cache to avoid 304 (Pinia auth bug)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("GET /api/users/me called for user: {}", principal != null ? principal.getUsername() : "null");

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String role = user.getRoleId() == 1L ? "ADMIN" : "USER";

        UserResponseDTO response = UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .avatarURL(user.getAvatarURL())
                .role(role)
                .isVerified(user.isVerified())
                .build();

        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.noStore()) // ⭐ FIX 304
                .body(response);
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid UpdateProfileRequest request) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok("Profile updated successfully");
    }

    /**
     * Change password
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid ChangePasswordRequest request) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok("Password changed successfully");
    }
}
