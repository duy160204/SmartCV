package com.example.SmartCV.modules.cv.dto.projection;

import java.time.LocalDateTime;
import com.example.SmartCV.modules.cv.domain.CVStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lightweight CV list DTO.
 * Populated via a JPQL constructor query in CVRepository so we can
 * JOIN cv -> template and expose template.thumbnailUrl in one query.
 */
@Getter
@AllArgsConstructor
public class CVListProjection {

    private Long id;
    private String title;
    private Long templateId;
    private CVStatus status;
    private Boolean isPublic;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** Pulled from the joined Template row; null if template has no thumbnail. */
    private String templateThumbnailUrl;
}
