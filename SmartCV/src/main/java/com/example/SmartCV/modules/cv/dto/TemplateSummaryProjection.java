package com.example.SmartCV.modules.cv.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import java.time.LocalDateTime;

public interface TemplateSummaryProjection {
    Long getId();

    String getCode();

    String getName();

    String getDescription();

    String getThumbnailUrl();

    PlanType getPlanRequired();

    Boolean getIsActive();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
