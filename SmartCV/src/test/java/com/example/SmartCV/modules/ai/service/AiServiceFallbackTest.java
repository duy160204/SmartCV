package com.example.SmartCV.modules.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @MockBean
    private AiProvider openAiProvider;

    @MockBean
    private AiRateLimiter aiRateLimiter;

    @Autowired
    private AiService aiService;

    @BeforeEach
    void setUp() {
        when(aiProviderFactory.getPrimaryProvider()).thenReturn(openAiProvider);
    }

    @Test
    void testOpenAiSuccess() {
        UnifiedAiResponse expectedResponse = new UnifiedAiResponse("OpenAI Response", AiProviderType.OPENAI, 100);
        when(openAiProvider.chat(any(UnifiedAiRequest.class))).thenReturn(expectedResponse);

        String result = aiService.chatWithCv("CV Content", "Question");

        assertEquals("OpenAI Response", result);
        verify(openAiProvider, times(1)).chat(any());
    }

    @Test
    void testOpenAiFailure_GenericFallback() {
        // Simulating OpenAI failure
        when(openAiProvider.chat(any(UnifiedAiRequest.class))).thenThrow(new RuntimeException("OpenAI Down"));

        // The fallback is managed by Resilience4j. In @SpringBootTest, it should trigger the fallback method.
        String result = aiService.chatWithCv("CV Content", "Question");

        assertEquals("The AI assistant is temporarily unavailable. Please try again in a few minutes.", result);
        verify(openAiProvider, times(1)).chat(any());
    }
}
