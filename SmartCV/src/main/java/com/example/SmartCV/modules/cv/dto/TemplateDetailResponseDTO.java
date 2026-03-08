package com.example.SmartCV.modules.cv.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetailResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String thumbnailUrl;
    private String previewContent;
    private String fullContent;
    private String configJson;
    private PlanType planRequired;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
