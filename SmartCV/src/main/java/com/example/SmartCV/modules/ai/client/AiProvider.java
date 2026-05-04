package com.example.SmartCV.modules.ai.client;

/**
 * Abstract interface for AI Providers.
 * Consolidated on OpenAI.
 */
import com.example.SmartCV.modules.ai.dto.UnifiedAiRequest;
import com.example.SmartCV.modules.ai.dto.UnifiedAiResponse;

/**
 * Abstract interface for AI Providers.
 */
public interface AiProvider {
    /**
     * Send a unified prompt to the AI provider.
     * 
     * @param request The unified request containing system and user messages.
     * @return The unified response containing content and metadata.
     */
    UnifiedAiResponse chat(UnifiedAiRequest request);

    /**
     * Get the type of this provider.
     * 
     * @return AiProviderType
     */
    AiProviderType getType();
}
