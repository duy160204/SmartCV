package com.example.SmartCV.modules.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.admin.service.AdminTemplateService;
import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/templates")
@RequiredArgsConstructor
public class AdminTemplateController {

    private final AdminTemplateService adminTemplateService;

    // =========================
    // CREATE TEMPLATE
    // =========================
    @PostMapping
    public ResponseEntity<?> createTemplate(
            @RequestParam String name,
            @RequestParam(required = false) String thumbnailUrl,
            @RequestParam String previewContent,
            @RequestParam String fullContent,
            @RequestParam PlanType planRequired
    ) {
        Template template = adminTemplateService.createTemplate(
                name,
                thumbnailUrl,
                previewContent,
                fullContent,
                planRequired
        );

        return ResponseEntity.ok(template);
    }

    // =========================
    // UPDATE TEMPLATE
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String thumbnailUrl,
            @RequestParam String previewContent,
            @RequestParam String fullContent,
            @RequestParam PlanType planRequired
    ) {
        Template template = adminTemplateService.updateTemplate(
                id,
                name,
                thumbnailUrl,
                previewContent,
                fullContent,
                planRequired
        );

        return ResponseEntity.ok(template);
    }

    // =========================
    // DISABLE TEMPLATE
    // =========================
    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableTemplate(@PathVariable Long id) {

        adminTemplateService.disableTemplate(id);

        return ResponseEntity.ok("Template disabled successfully");
    }

    // =========================
    // ENABLE TEMPLATE
    // =========================
    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableTemplate(@PathVariable Long id) {

        adminTemplateService.enableTemplate(id);

        return ResponseEntity.ok("Template enabled successfully");
    }

    // =========================
    // DELETE TEMPLATE
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {

        adminTemplateService.deleteTemplate(id);

        return ResponseEntity.ok("Template deleted successfully");
    }

    // =========================
    // GET ALL TEMPLATES (ADMIN)
    // =========================
    @GetMapping
    public ResponseEntity<?> getAllTemplates() {
        return ResponseEntity.ok(adminTemplateService.getAllTemplates());
    }

    // =========================
    // GET TEMPLATE DETAIL
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<?> getTemplateDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminTemplateService.getTemplateDetail(id));
    }
}
