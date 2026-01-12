package com.example.SmartCV.modules.admin.dto;

import java.time.LocalDateTime;

import com.example.SmartCV.modules.cv.domain.CVStatus;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCVDetailResponse {

    private Long id;
    private Long userId;
    private String title;
    private Long templateId;
    private String content;
    private CVStatus status;
    private Boolean isPublic;
    private Boolean isLocked;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
