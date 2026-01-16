package com.example.SmartCV.modules.admin.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for payment search/filter operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchDTO {

    private Long userId;
    private LocalDate from;
    private LocalDate to;

    // Default values
    public LocalDate getFromOrDefault() {
        return from != null ? from : LocalDate.now().minusDays(30);
    }

    public LocalDate getToOrDefault() {
        return to != null ? to : LocalDate.now();
    }
}
