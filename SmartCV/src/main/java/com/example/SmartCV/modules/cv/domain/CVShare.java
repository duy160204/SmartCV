package com.example.SmartCV.modules.cv.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "cv_shares",
    indexes = {
        @Index(name = "idx_cv_share_uuid", columnList = "share_uuid"),
        @Index(name = "idx_cv_share_cv_id", columnList = "cv_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cv_id", nullable = false)
    private Long cvId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "share_uuid", nullable = false, unique = true, length = 100)
    private String shareUuid;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {
        if (expiresAt == null) return false;
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
