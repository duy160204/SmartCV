package com.example.SmartCV.modules.admin.dto;

import java.time.LocalDate;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserListResponse {

    private Long id;
    private String email;

    private boolean verified;
    private boolean locked;

    private PlanType plan;

    private LocalDate createdAt;
}
