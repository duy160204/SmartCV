package com.example.SmartCV.modules.ai.dto;

import com.example.SmartCV.modules.ai.client.AiProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnifiedAiResponse {
    /**
     * The raw text content returned by the AI.
     */
    private String content;

    /**
     * The provider that successfully handled the request.
     */
    private AiProviderType provider;

    /**
     * Execution time in milliseconds.
     */
    private long latencyMs;

    /**
     * Default constructor for simple text response.
     */
    public UnifiedAiResponse(String content) {
        this.content = content;
    }
}
