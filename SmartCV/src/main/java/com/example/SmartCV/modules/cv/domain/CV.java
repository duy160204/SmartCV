package com.example.SmartCV.modules.cv.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cv")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_version")
    private Integer templateVersion;

    @Column(name = "template_snapshot", columnDefinition = "LONGTEXT")
    private String templateSnapshot;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CVStatus status;

    @Builder.Default
    @Column(name = "is_public")
    private Boolean isPublic = false;

    // 👉 THÊM DÒNG NÀY
    @Builder.Default
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @Builder.Default
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
