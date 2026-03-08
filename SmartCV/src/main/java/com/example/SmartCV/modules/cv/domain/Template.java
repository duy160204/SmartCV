package com.example.SmartCV.modules.cv.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.SmartCV.modules.subscription.domain.PlanType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "templates", indexes = {
        @Index(name = "idx_template_active_plan", columnList = "is_active, plan_required")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // VD: MODERN_01

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "preview_content", columnDefinition = "TEXT")
    private String previewContent;

    @Column(name = "full_content", columnDefinition = "LONGTEXT")
    private String fullContent;

    @Column(name = "config_json", columnDefinition = "LONGTEXT")
    private String configJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_required", nullable = false)
    private PlanType planRequired;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}
