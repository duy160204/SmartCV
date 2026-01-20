package com.example.SmartCV.modules.cv.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.cv.dto.PublicCVResponseDTO;
import com.example.SmartCV.modules.cv.repository.CVRepository;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.example.SmartCV.modules.subscription.domain.UsageType;
import com.example.SmartCV.modules.subscription.repository.SubscriptionUsageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCVService {

    private final SubscriptionUsageRepository subscriptionUsageRepository;
    private final CVRepository cvRepository;
    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    public PublicCVResponseDTO getPublicCV(String token) {
        // 1. Find Usage by Token
        var usage = subscriptionUsageRepository.findByShareUuid(token)
                .orElseThrow(() -> new BusinessException("Invalid or expired link", HttpStatus.NOT_FOUND));

        // 2. Validate Usage Type
        if (usage.getUsageType() != UsageType.SHARE) {
            throw new BusinessException("Invalid link type", HttpStatus.BAD_REQUEST);
        }

        // 3. Check Expiry
        if (usage.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("This link has expired", HttpStatus.GONE);
        }

        // 4. Fetch CV
        CV cv = cvRepository.findById(usage.getCvId())
                .orElseThrow(() -> new BusinessException("CV not found", HttpStatus.NOT_FOUND));

        // 5. Fetch Template
        Template template = templateRepository.findById(cv.getTemplateId())
                .orElseThrow(() -> new BusinessException("Template not found", HttpStatus.NOT_FOUND));

        // 6. Build DTO
        Object contentObj = null;
        try {
            // Assume content is stored as JSON String in DB, but we want to return Object
            // to frontend
            // If it's already an object in domain (it's String in Entity), parse it.
            contentObj = objectMapper.readValue(cv.getContent(), Object.class);
        } catch (Exception e) {
            // Fallback if parsing fails or if it's already object
            contentObj = cv.getContent();
        }

        // Parse Template FullContent (String JSON) to get HTML/CSS
        // Template.fullContent = { html: "...", css: "...", ... }
        String html = "";
        String css = "";
        try {
            var node = objectMapper.readTree(template.getFullContent());
            if (node.has("html"))
                html = node.get("html").asText();
            if (node.has("css"))
                css = node.get("css").asText();
        } catch (Exception e) {
            // Fallback
            html = "<h1>Error loading template</h1>";
        }

        return PublicCVResponseDTO.builder()
                .title(cv.getTitle())
                .content(contentObj)
                .html(html)
                .css(css)
                .build();
    }

    public byte[] downloadPublicCV(String token) {
        // 1. Reuse existing logic to fetch data
        PublicCVResponseDTO cvData = getPublicCV(token);

        try {
            // 2. Compile Template with Handlebars
            com.github.jknack.handlebars.Handlebars handlebars = new com.github.jknack.handlebars.Handlebars();
            com.github.jknack.handlebars.Template template = handlebars.compileInline(cvData.getHtml());

            // Render HTML body
            String bodyHtml = template.apply(cvData.getContent());

            // 3. Construct Full HTML for PDF
            String fullHtml = String.format("""
                    <html>
                    <head>
                        <style>
                            @page { margin: 0; }
                            body { margin: 0; font-family: sans-serif; }
                            %s
                        </style>
                    </head>
                    <body>
                        %s
                    </body>
                    </html>
                    """, cvData.getCss(), bodyHtml);

            // 4. Convert to PDF
            try (java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream()) {
                com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(fullHtml, "");
                builder.toStream(os);
                builder.run();
                return os.toByteArray();
            }

        } catch (Exception e) {
            throw new BusinessException("Failed to generate PDF: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
