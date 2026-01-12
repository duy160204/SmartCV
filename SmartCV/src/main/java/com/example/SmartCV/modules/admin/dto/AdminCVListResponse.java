package com.example.SmartCV.modules.admin.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCVListResponse {

    private Long id;
    private Long userId;
    private String title;
    private Long templateId;
    private Boolean isPublic;
    private Boolean isLocked;
    private LocalDateTime createdAt;
}
