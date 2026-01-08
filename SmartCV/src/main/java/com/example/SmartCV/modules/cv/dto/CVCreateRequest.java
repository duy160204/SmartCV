package com.example.SmartCV.modules.cv.dto;

import lombok.Data;

@Data
public class CVCreateRequest {
    private Long templateId;
    private String title;
    private String content;
}
