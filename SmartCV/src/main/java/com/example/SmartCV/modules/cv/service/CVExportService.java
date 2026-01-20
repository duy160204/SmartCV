package com.example.SmartCV.modules.cv.service;

import java.io.ByteArrayOutputStream;
// IO Exception removed

import org.springframework.stereotype.Service;

import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class CVExportService {

    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;
    private final Handlebars handlebars = new Handlebars();

    /**
     * Export CV -> PDF
     */
    public byte[] exportToPdf(CV cv) {

        try {
            // 1. Fetch Template
            Template template = templateRepository.findById(cv.getTemplateId())
                    .orElseThrow(() -> new BusinessException("Template not found", HttpStatus.NOT_FOUND));

            // 2. Parse Template HTML/CSS
            String htmlTemplate = "";
            String css = "";
            try {
                JsonNode node = objectMapper.readTree(template.getFullContent());
                if (node.has("html"))
                    htmlTemplate = node.get("html").asText();
                if (node.has("css"))
                    css = node.get("css").asText();
            } catch (Exception e) {
                throw new BusinessException("Invalid template format", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 3. Parse CV Content (JSON String -> Object)
            Object contentObj = objectMapper.readValue(cv.getContent(), Object.class);

            // 4. Render HTML via Handlebars
            com.github.jknack.handlebars.Template hbsTemplate = handlebars.compileInline(htmlTemplate);
            String bodyHtml = hbsTemplate.apply(contentObj);

            // 5. Assemble Full HTML for PDF
            String fullHtml = buildFullHtml(bodyHtml, css);

            // 6. Convert to PDF
            return htmlToPdf(fullHtml);

        } catch (Exception e) {
            throw new RuntimeException("Failed to export PDF: " + e.getMessage(), e);
        }
    }

    private String buildFullHtml(String bodyHtml, String css) {
        return "<html><head><style>" +
                "body { margin: 0; padding: 0; }" +
                css +
                "</style></head><body>" +
                bodyHtml +
                "</body></html>";
    }

    private byte[] htmlToPdf(String html) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(outputStream);
        builder.useFastMode();
        builder.run();
        return outputStream.toByteArray();
    }
}
