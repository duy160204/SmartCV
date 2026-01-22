package com.example.SmartCV.modules.ai.client;

/**
 * Abstract interface for AI Providers.
 * This allows switching between OpenAI, Gemini, etc. without changing business
 * logic.
 */
public interface AiProvider {
    /**
     * Send a prompt to the AI provider and get the text response.
     * 
     * @param prompt The prompt to send.
     * @return The text response from the AI.
     */
    String chat(String prompt);
}
