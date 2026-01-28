package com.example.SmartCV.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnifiedAiRequest {
    /**
     * Helper text defining the role, system instructions, and JSON requirements.
     */
    private String systemMessage;

    /**
     * The actual user query or CV content to process.
     */
    private String userMessage;
}
