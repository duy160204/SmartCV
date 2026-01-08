package com.example.SmartCV.modules.cv.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "cv_favorites",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_template", columnNames = {"user_id", "template_id"})
    },
    indexes = {
        @Index(name = "idx_cv_fav_user", columnList = "user_id"),
        @Index(name = "idx_cv_fav_template", columnList = "template_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
