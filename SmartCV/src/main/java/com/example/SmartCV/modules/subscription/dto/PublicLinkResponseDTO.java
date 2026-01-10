package com.example.SmartCV.modules.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PublicLinkResponseDTO {
    private String publicUrl;
    private String expiredAt;
}
