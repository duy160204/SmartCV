package com.example.SmartCV.modules.admin.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequestDTO {

    @NotBlank(message = "Template name is required")
    private String name;

    private String thumbnailUrl;

    @NotBlank(message = "Preview content is required")
    private String previewContent;

    @NotBlank(message = "Full content is required")
    private String fullContent;

    @NotNull(message = "Plan type is required")
    private PlanType planRequired;
}
