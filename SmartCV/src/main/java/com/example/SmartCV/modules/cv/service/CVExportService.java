package com.example.SmartCV.modules.cv.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.SmartCV.modules.cv.domain.CV;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CVExportService {

    private final TemplateEngine templateEngine;

    /**
     * Export CV -> PDF
     */
    public byte[] exportToPdf(CV cv) {

        try {
            // 1️⃣ Render HTML từ Thymeleaf
            String html = renderHtml(cv);

            // 2️⃣ Convert HTML -> PDF
            return htmlToPdf(html);

        } catch (Exception e) {
            throw new RuntimeException("Không thể export CV sang PDF", e);
        }
    }

    // =========================
    // Render HTML
    // =========================

    private String renderHtml(CV cv) {

        Context context = new Context();
        context.setVariable("cv", cv);

        // 👉 chọn template theo cv.templateId (sau này mở rộng)
        return templateEngine.process(
                "cv/cv-default",
                context
        );
    }

    // =========================
    // HTML -> PDF
    // =========================

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
