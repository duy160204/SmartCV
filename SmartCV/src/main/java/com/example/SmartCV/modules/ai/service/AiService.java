package com.example.SmartCV.modules.ai.service;

import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.ai.client.AiProvider;
import com.example.SmartCV.modules.ai.client.AiProviderFactory;
import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiProviderFactory aiProviderFactory;

    public String chatWithCv(String cvContent, String userMessage) {
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n"
                        + AiPrompts.CV_REVIEW_SYSTEM_PROMPT.formatted(cvContent, userMessage)) // Using formatted prompt
                                                                                               // structure
                .userMessage(userMessage) // In the prompt above, user question is already embedded?
                // Wait, the prompt structure in AiPrompts.java is mixing system and user
                // context.
                // Let's optimize:
                // AiPrompts.CV_REVIEW_SYSTEM_PROMPT takes %s %s (content, question).
                // Ideally, we separate them. But for now, let's keep the existing logic and map
                // it to System Message.
                // ACTUALLY: The request requires strict separation if we want to use 'system'
                // role in OpenAI.
                // Let's refine the prompt construction slightly.
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n"
                        + "Bạn là chuyên gia tuyển dụng và hướng nghiệp. Nhiệm vụ của bạn là đánh giá CV và đưa ra góp ý thực tế, dễ hiểu, chi tiết.")
                .userMessage("=== NỘI DUNG CV ===\n" + cvContent + "\n\n=== CÂU HỎI CỦA NGƯỜI DÙNG ===\n" + userMessage)
                .build();

        return executeWithFallback(request);
    }

    public String generateCvContent(String prompt, String templateConfigJson) {
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.GENERATE_CV_PROMPT)
                .userMessage("=== USER BACKGROUND ===\n" + prompt + "\n\n=== SECTION CONFIG ===\n" + templateConfigJson)
                .build();
        return executeWithFallback(request);
    }

    public String improveText(String text, String instruction) {
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.IMPROVE_TEXT_PROMPT)
                .userMessage("=== INSTRUCTION ===\n" + instruction + "\n\n=== TEXT TO IMPROVE ===\n" + text)
                .build();
        return executeWithFallback(request);
    }

    public String buildTemplateFromImage(String imageUrl) {
        UnifiedAiRequest request = UnifiedAiRequest.builder()
                .systemMessage(AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.BUILD_TEMPLATE_PROMPT)
                .userMessage("Convert the image according to the instructions.")
                .imageUrl(imageUrl)
                .build();
        return executeWithFallback(request);
    }

    private String executeWithFallback(UnifiedAiRequest request) {
        try {
            return executeChat(aiProviderFactory.getPrimaryProvider(), request);
        } catch (Exception e) {
            log.error("Primary AI Provider ({}) failed: {}", aiProviderFactory.getPrimaryType(), e.getMessage());
            try {
                log.info("Switching to Fallback AI Provider...");
                return executeChat(aiProviderFactory.getFallbackProvider(), request);
            } catch (Exception ex) {
                log.error("Fallback AI Provider also failed: {}", ex.getMessage());
                throw new com.example.SmartCV.common.exception.BusinessException(
                        "All AI services unavailable. Please try again later.",
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE);
            }
        }
    }

    private String executeChat(AiProvider provider, UnifiedAiRequest request) {
        if (provider == null) {
            throw new RuntimeException("AI Provider not configured.");
        }
        UnifiedAiResponse response = provider.chat(request);
        log.info("AI Request processed by {} in {}ms", response.getProvider(), response.getLatencyMs());
        return response.getContent();
    }
}
