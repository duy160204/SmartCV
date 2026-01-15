package com.example.SmartCV.modules.ai.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    private Long cvId;
    private String message;
}
