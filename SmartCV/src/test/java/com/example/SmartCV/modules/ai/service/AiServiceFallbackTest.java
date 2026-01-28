package com.example.SmartCV.modules.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.SmartCV.modules.ai.client.AiProvider;
import com.example.SmartCV.modules.ai.client.AiProviderFactory;
import com.example.SmartCV.modules.ai.client.AiProviderType;
import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;

class AiServiceFallbackTest {

    private AiProviderFactory aiProviderFactory;
    private AiService aiService;
    private AiProvider geminiProvider;
    private AiProvider openAiProvider;

    @BeforeEach
    void setUp() {
        aiProviderFactory = mock(AiProviderFactory.class);
        geminiProvider = mock(AiProvider.class);
        openAiProvider = mock(AiProvider.class);

        // Default Config: Primary = GEMINI, Fallback = OPENAI
        when(aiProviderFactory.getPrimaryProvider()).thenReturn(geminiProvider);
        when(aiProviderFactory.getFallbackProvider()).thenReturn(openAiProvider);
        when(aiProviderFactory.getPrimaryType()).thenReturn(AiProviderType.GEMINI);

        aiService = new AiService(aiProviderFactory);
    }

    @Test
    void testPrimarySuccess() {
        // Arrange
        UnifiedAiResponse expectedResponse = new UnifiedAiResponse("Gemini Response", AiProviderType.GEMINI, 100);
        when(geminiProvider.chat(any(UnifiedAiRequest.class))).thenReturn(expectedResponse);

        // Act
        String result = aiService.chatWithCv("CV Content", "Question");

        // Assert
        assertEquals("Gemini Response", result);
        verify(geminiProvider, times(1)).chat(any());
        verify(openAiProvider, times(0)).chat(any());
    }

    @Test
    void testPrimaryFail_FallbackSuccess() {
        // Arrange
        when(geminiProvider.chat(any(UnifiedAiRequest.class))).thenThrow(new RuntimeException("Gemini Down"));

        UnifiedAiResponse fallbackResponse = new UnifiedAiResponse("OpenAI Response", AiProviderType.OPENAI, 150);
        when(openAiProvider.chat(any(UnifiedAiRequest.class))).thenReturn(fallbackResponse);

        // Act
        String result = aiService.chatWithCv("CV Content", "Question");

        // Assert
        assertEquals("OpenAI Response", result);
        verify(geminiProvider, times(1)).chat(any()); // Tried Primary
        verify(openAiProvider, times(1)).chat(any()); // Switched to Fallback
    }

    @Test
    void testBothFail() {
        // Arrange
        when(geminiProvider.chat(any(UnifiedAiRequest.class))).thenThrow(new RuntimeException("Gemini Down"));
        when(openAiProvider.chat(any(UnifiedAiRequest.class))).thenThrow(new RuntimeException("OpenAI Down"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> aiService.chatWithCv("CV Content", "Question"));

        verify(geminiProvider, times(1)).chat(any());
        verify(openAiProvider, times(1)).chat(any());
    }
}
