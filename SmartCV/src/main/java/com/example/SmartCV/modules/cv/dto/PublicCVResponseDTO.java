package com.example.SmartCV.modules.cv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicCVResponseDTO {
    private String title;
    private Object content; // JSON Data
    private String html; // Template HTML
    private String css; // Template CSS
}
