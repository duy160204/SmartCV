package com.example.SmartCV.modules.cv.dto.projection;

import java.time.LocalDateTime;
import com.example.SmartCV.modules.cv.domain.CVStatus;

public interface CVListProjection {
    Long getId();

    String getTitle();

    Long getTemplateId();

    CVStatus getStatus();

    Boolean getIsPublic();

    Long getViewCount();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
