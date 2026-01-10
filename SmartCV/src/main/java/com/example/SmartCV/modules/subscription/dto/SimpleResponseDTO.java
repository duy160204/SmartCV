package com.example.SmartCV.modules.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleResponseDTO {
    private String message;
    private Object data;

    public SimpleResponseDTO(String message) {
        this.message = message;
    }
}
