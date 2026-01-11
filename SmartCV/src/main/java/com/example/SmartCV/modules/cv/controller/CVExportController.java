package com.example.SmartCV.modules.cv.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.cv.service.CVService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cv/export")
@RequiredArgsConstructor
public class CVExportController {

    private final CVService cvService;

    // =========================
    // EXPORT CV TO PDF
    // =========================
    @GetMapping("/{cvId}/pdf")
    public ResponseEntity<byte[]> exportCvToPdf(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long cvId
    ) {
        Long userId = principal.getId();

        byte[] pdfBytes = cvService.downloadCV(userId, cvId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=cv-" + cvId + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
