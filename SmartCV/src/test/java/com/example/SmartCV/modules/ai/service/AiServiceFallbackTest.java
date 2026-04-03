package com.example.SmartCV.modules.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.SmartCV.modules.ai.client.AiProvider;
import com.example.SmartCV.modules.ai.client.AiProviderFactory;
import com.example.SmartCV.modules.ai.client.AiProviderType;
import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;

@SpringBootTest
class AiServiceFallbackTest {

    @MockBean
    private AiProviderFactory aiProviderFactory;

    @MockBean(name = "primaryAiProvider")
    private AiProvider geminiProvider;

    @MockBean(name = "fallbackAiProvider")
    private AiProvider openAiProvider;

    @MockBean
    private AiRateLimiter aiRateLimiter;

    @Autowired
    private AiService aiService;

    @BeforeEach
    void setUp() {
        when(aiProviderFactory.getPrimaryProvider()).thenReturn(geminiProvider);
        when(aiProviderFactory.getFallbackProvider()).thenReturn(openAiProvider);
    }

    @Test
    void testPrimarySuccess() {
        UnifiedAiResponse expectedResponse = new UnifiedAiResponse("Gemini Response", AiProviderType.GEMINI, 100);
        when(geminiProvider.chat(any(UnifiedAiRequest.class))).thenReturn(expectedResponse);

        String result = aiService.chatWithCv("CV Content", "Question");

        assertEquals("Gemini Response", result);
        verify(geminiProvider, times(1)).chat(any());
        verify(openAiProvider, times(0)).chat(any());
    }

    @Test
    void testPrimaryFail_FallbackSuccess() {
        when(geminiProvider.chat(any(UnifiedAiRequest.class))).thenThrow(new RuntimeException("Gemini Down"));

        UnifiedAiResponse fallbackResponse = new UnifiedAiResponse("OpenAI Response", AiProviderType.OPENAI, 150);
        when(openAiProvider.chat(any(UnifiedAiRequest.class))).thenReturn(fallbackResponse);

        String result = aiService.chatWithCv("CV Content", "Question");

        assertEquals("OpenAI Response", result);
        verify(geminiProvider, times(1)).chat(any());
        verify(openAiProvider, times(1)).chat(any());
    }

    @Test
    void testBothFail() {
        when(geminiProvider.chat(any(UnifiedAiRequest.class))).thenThrow(new RuntimeException("Gemini Down"));
        when(openAiProvider.chat(any(UnifiedAiRequest.class))).thenThrow(new RuntimeException("OpenAI Down"));

        assertThrows(RuntimeException.class, () -> aiService.chatWithCv("CV Content", "Question"));

        verify(geminiProvider, times(1)).chat(any());
        verify(openAiProvider, times(1)).chat(any());
    }
}
