package com.example.SmartCV.modules.cv.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.domain.Template;
import com.example.SmartCV.modules.cv.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PuppeteerPdfService {

    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;
    private final Handlebars handlebars = new Handlebars();

    public byte[] renderWithPuppeteer(CV cv) {
        try {
            Template template = templateRepository.findById(cv.getTemplateId())
                    .orElseThrow(() -> new BusinessException("Template not found", HttpStatus.NOT_FOUND));

            String htmlTemplate = "";
            String css = "";

            try {
                JsonNode node = objectMapper.readTree(template.getFullContent());
                if (node.has("html")) {
                    htmlTemplate = node.get("html").asText();
                } else {
                    htmlTemplate = template.getFullContent();
                }
                if (node.has("css")) {
                    css = node.get("css").asText();
                }
            } catch (Exception e) {
                htmlTemplate = template.getFullContent();
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

            com.github.jknack.handlebars.Template hbsTemplate = handlebars.compileInline(htmlTemplate);
            String bodyHtml = hbsTemplate.apply(contentObj);

            String customFontCss = 
                "@font-face {\n" +
                "  font-family: 'Noto Sans';\n" +
                "  src: url('http://localhost:8080/fonts/NotoSans-Regular.ttf');\n" +
                "}\n" +
                "body {\n" +
                "  font-family: 'Noto Sans', sans-serif;\n" +
                "}\n" +
                "@page { size: A4; margin: 0; }\n" +
                "html, body { width: 210mm; min-height: 297mm; margin: 0; padding: 0; }\n";

            String fullHtml = "<!DOCTYPE html>\n" +
                    "<html>\n<head>\n<meta charset=\"UTF-8\"/>\n" +
                    "<style>\n" + customFontCss + "\n" + css + "\n</style>\n</head>\n<body>\n" +
                    bodyHtml + "\n</body>\n</html>";

            HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:3002/render-pdf").openConnection();
            
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            java.util.Map<String, String> bodyMap = new java.util.HashMap<>();
            bodyMap.put("html", fullHtml);
            String bodyReq = objectMapper.writeValueAsString(bodyMap);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyReq.getBytes(StandardCharsets.UTF_8));
            }

            return conn.getInputStream().readAllBytes();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Puppeteer PDF render failed", e);
            throw new RuntimeException("Puppeteer PDF render failed: " + e.getMessage(), e);
        }
    }
}
