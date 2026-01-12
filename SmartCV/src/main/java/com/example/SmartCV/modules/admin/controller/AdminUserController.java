package com.example.SmartCV.modules.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.admin.dto.AdminUserDetailResponse;
import com.example.SmartCV.modules.admin.dto.AdminUserListResponse;
import com.example.SmartCV.modules.admin.service.AdminUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // =========================
    // LIST USERS
    // =========================
    @GetMapping
    public ResponseEntity<List<AdminUserListResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    // =========================
    // USER DETAIL
    // =========================
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(adminUserService.getUserDetail(userId));
    }

    // =========================
    // LOCK USER
    // =========================
    @PutMapping("/{userId}/lock")
    public ResponseEntity<Void> lockUser(@PathVariable Long userId) {
        adminUserService.lockUser(userId);
        return ResponseEntity.ok().build();
    }

    // =========================
    // UNLOCK USER
    // =========================
    @PutMapping("/{userId}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable Long userId) {
        adminUserService.unlockUser(userId);
        return ResponseEntity.ok().build();
    }
}
