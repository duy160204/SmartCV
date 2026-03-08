package com.example.SmartCV.modules.ai.dto;

import lombok.Data;

@Data
public class AiGenerateRequest {
    private String prompt;
    private String templateConfigJson;
}
