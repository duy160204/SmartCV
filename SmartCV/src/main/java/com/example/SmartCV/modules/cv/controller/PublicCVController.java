package com.example.SmartCV.modules.cv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartCV.modules.cv.dto.PublicCVResponseDTO;
import com.example.SmartCV.modules.cv.service.PublicCVService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/cv")
@RequiredArgsConstructor
public class PublicCVController {

    private final PublicCVService publicCVService;

    @GetMapping("/{token}")
    public ResponseEntity<PublicCVResponseDTO> getPublicCV(@PathVariable String token) {
        return ResponseEntity.ok(publicCVService.getPublicCV(token));
    }

    @GetMapping("/{token}/download")
    public ResponseEntity<byte[]> downloadPublicCV(@PathVariable String token) {
        byte[] pdfContent = publicCVService.downloadPublicCV(token);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                        org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cv.pdf\"")
                .body(pdfContent);
    }
}
