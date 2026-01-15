package com.example.SmartCV.modules.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.user.dto.UserResponseDTO;
import com.example.SmartCV.modules.user.dto.ChangePasswordRequest;
import com.example.SmartCV.modules.user.dto.UpdateProfileRequest;
import com.example.SmartCV.modules.user.service.UserService;
import com.example.SmartCV.common.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Get current user profile (Session Rehydration)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String role = (user.getRoleId() == 1L) ? "ADMIN" : "USER";

        UserResponseDTO response = UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .avatarURL(user.getAvatarURL())
                .role(role)
                .isVerified(user.isVerified())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok("Password changed successfully");
    }
}
