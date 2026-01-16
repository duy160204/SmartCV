package com.example.SmartCV.modules.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.dto.ApiResponse;
import com.example.SmartCV.modules.admin.dto.TemplateRequestDTO;
import com.example.SmartCV.modules.admin.service.AdminTemplateService;
import com.example.SmartCV.modules.cv.domain.Template;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/templates")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminTemplateController {

    private final AdminTemplateService adminTemplateService;

    // =========================
    // CREATE TEMPLATE
    // =========================
    @PostMapping
    public ResponseEntity<ApiResponse<Template>> createTemplate(
            @Valid @RequestBody TemplateRequestDTO request) {
        Template template = adminTemplateService.createTemplate(
                request.getName(),
                request.getThumbnailUrl(),
                request.getPreviewContent(),
                request.getFullContent(),
                request.getPlanRequired());

        return ResponseEntity.ok(ApiResponse.success("Template created successfully", template));
    }

    // =========================
    // UPDATE TEMPLATE
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Template>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateRequestDTO request) {
        Template template = adminTemplateService.updateTemplate(
                id,
                request.getName(),
                request.getThumbnailUrl(),
                request.getPreviewContent(),
                request.getFullContent(),
                request.getPlanRequired());

        return ResponseEntity.ok(ApiResponse.success("Template updated successfully", template));
    }

    // =========================
    // DISABLE TEMPLATE
    // =========================
    @PutMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableTemplate(@PathVariable Long id) {
        adminTemplateService.disableTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template disabled successfully"));
    }

    // =========================
    // ENABLE TEMPLATE
    // =========================
    @PutMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableTemplate(@PathVariable Long id) {
        adminTemplateService.enableTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template enabled successfully"));
    }

    // =========================
    // DELETE TEMPLATE
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        adminTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully"));
    }

    // =========================
    // GET ALL TEMPLATES (ADMIN)
    // =========================
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success(adminTemplateService.getAllTemplates()));
    }

    // =========================
    // GET TEMPLATE DETAIL
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getTemplateDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminTemplateService.getTemplateDetail(id)));
    }
}
