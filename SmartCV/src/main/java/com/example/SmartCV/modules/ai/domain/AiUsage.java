package com.example.SmartCV.modules.ai.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tracks AI usage per user per day to prevent abuse
 * NOT plan-based - applies to all users equally
 */
@Entity
@Table(name = "ai_usage", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "usage_date" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "request_count", nullable = false)
    @Builder.Default
    private Integer requestCount = 0;

    @Column(name = "last_request_at")
    private LocalDateTime lastRequestAt;

    public void incrementCount() {
        this.requestCount++;
        this.lastRequestAt = LocalDateTime.now();
    }
}
