package com.example.SmartCV.modules.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.admin.dto.AdminDashboardResponse;
import com.example.SmartCV.modules.admin.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    // ==================================================
    // DASHBOARD OVERVIEW (CARDS)
    // ==================================================
    @GetMapping
    public ResponseEntity<AdminDashboardResponse> overview() {
        return ResponseEntity.ok(
                dashboardService.getDashboardOverview());
    }
}
