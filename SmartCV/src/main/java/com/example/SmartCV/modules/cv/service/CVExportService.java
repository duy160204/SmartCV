package com.example.SmartCV.modules.cv.service;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Base64;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CVExportService {

    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;
    private final Handlebars handlebars = new Handlebars();

    public byte[] exportToPdf(CV cv) {
        try {
            Template template = templateRepository.findById(cv.getTemplateId())
                    .orElseThrow(() -> new BusinessException("Template not found", HttpStatus.NOT_FOUND));

            String htmlTemplate = "";
            String css = "";

            try {
                JsonNode node = objectMapper.readTree(template.getFullContent());
                if (node.has("pdfHtml") && !node.get("pdfHtml").asText().isEmpty()) {
                    htmlTemplate = node.get("pdfHtml").asText();
                } else if (template.getPdfHtml() != null && !template.getPdfHtml().isEmpty()) {
                    htmlTemplate = template.getPdfHtml();
                } else if (node.has("html")) {
                    htmlTemplate = node.get("html").asText();
                } else {
                    htmlTemplate = template.getFullContent();
                }

                if (node.has("pdfCss") && !node.get("pdfCss").asText().isEmpty()) {
                    css = node.get("pdfCss").asText();
                } else if (template.getPdfCss() != null && !template.getPdfCss().isEmpty()) {
                    css = template.getPdfCss();
                } else if (node.has("css")) {
                    css = node.get("css").asText();
                }
            } catch (Exception e) {
                if (template.getPdfHtml() != null && !template.getPdfHtml().isEmpty()) {
                    htmlTemplate = template.getPdfHtml();
                    css = template.getPdfCss() != null ? template.getPdfCss() : "";
                } else {
                    htmlTemplate = template.getFullContent();
                }
            }

            Object contentObj;
            try {
                String raw = (cv.getDataJson() != null && !cv.getDataJson().isBlank())
                        ? cv.getDataJson()
                        : cv.getContent();
                contentObj = objectMapper.readValue(raw, Object.class);
            } catch (Exception e) {
                contentObj = cv.getContent();
            }

            handlebars.registerHelper("limitWords", (str, options) -> {
                if (!(str instanceof String s))
                    return "";
                String[] words = s.split("\\s+");
                int limit = options.param(0, 80);
                if (words.length > limit) {
                    return String.join(" ", java.util.Arrays.copyOf(words, limit)) + "...";
                }
                return s;
            });

            // 1. Render Handlebars
            com.github.jknack.handlebars.Template hbsTemplate = handlebars.compileInline(htmlTemplate);
            String bodyHtml = hbsTemplate.apply(contentObj);

            // 2. Inject CSS
            String customPdfCss = "@page {\n" +
                    "  size: A4;\n" +
                    "  margin: 0;\n" +
                    "}\n" +
                    "html, body {\n" +
                    "  width: 210mm;\n" +
                    "  margin: 0;\n" +
                    "  padding: 0;\n" +
                    "}\n" +
                    "html, body, * {\n" +
                    "  font-family: 'Noto Sans' !important;\n" +
                    "}\n" +
                    ".flex, [style*=\"flex\"] {\n" +
                    "  display: block !important;\n" +
                    "}\n" +
                    "* {\n" +
                    "  box-sizing: border-box;\n" +
                    "  max-width: 100%;\n" +
                    "}\n" +
                    "body {\n" +
                    "  line-height: 1.4;\n" +
                    "}\n" +
                    "p, div, span {\n" +
                    "  word-break: break-word;\n" +
                    "  white-space: normal;\n" +
                    "}\n" +
                    "img {\n" +
                    "  max-width: 100%;\n" +
                    "  height: auto;\n" +
                    "}\n";

            String fullHtml = "<!DOCTYPE html>\n" +
                    "<html>\n<head>\n" +
                    "<style>\n" + customPdfCss + "\n" + css + "\n</style>\n</head>\n<body>\n" +
                    bodyHtml + "\n</body>\n</html>";

            return exportPdf(fullHtml);

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Failed to export PDF", e);
            throw new RuntimeException("Failed to export PDF: " + e.getMessage(), e);
        }
    }

    private String convertToXhtml(String html) {
        Document doc = Jsoup.parse(html);

        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml);

        return doc.html();
    }

    private String inlineImages(String html) {
        Document doc = Jsoup.parse(html);

        for (Element img : doc.select("img")) {
            String src = img.attr("src");

            try {
                if (src.startsWith("http")) {
                    byte[] bytes = new URL(src).openStream().readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    img.attr("src", "data:image/png;base64," + base64);
                }
            } catch (Exception ignored) {
            }
        }

        return doc.html();
    }

    public byte[] exportPdf(String html) {
        try {
            html = inlineImages(html);

            if (!html.contains("charset") && !html.contains("CHARSET")) {
                html = html.replace("<head>", "<head><meta charset=\"UTF-8\"/>");
            }

            html = convertToXhtml(html);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);

            // FONT
            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf"),
                    "Noto Sans",
                    400,
                    FontStyle.NORMAL,
                    true);

            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/NotoSans-Bold.ttf"),
                    "Noto Sans",
                    700,
                    FontStyle.NORMAL,
                    true);

            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/NotoSans-Italic.ttf"),
                    "Noto Sans",
                    400,
                    FontStyle.ITALIC,
                    true);

            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/NotoSans-BoldItalic.ttf"),
                    "Noto Sans",
                    700,
                    FontStyle.ITALIC,
                    true);

            builder.toStream(out);
            builder.run();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
