package com.example.SmartCV.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for CV admin actions (lock/unlock/delete)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVActionRequestDTO {

    private String reason;
}
