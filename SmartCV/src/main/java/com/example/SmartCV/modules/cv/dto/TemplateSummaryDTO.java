package com.example.SmartCV.modules.cv.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSummaryDTO implements Serializable {
    private Long id;
    
    @Builder.Default
    private String code = "";
    
    @Builder.Default
    private String name = "";
    
    @Builder.Default
    private String description = "";
    
    @Builder.Default
    private String thumbnailUrl = "";
    
    private PlanType planRequired;
    
    @Builder.Default
    private Boolean isActive = false;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
