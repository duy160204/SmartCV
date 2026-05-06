package com.example.SmartCV.modules.auth.domain;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @PrePersist
    @PreUpdate
    private void normalizeEmail() {
        if (email != null) {
            this.email = email.trim().toLowerCase();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String email;

    private String username;

    private String password;

    private String avatarURL;

    @Column(name = "role_id", insertable = false, updatable = false)
    private Long roleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    // ===== CORE STATUS =====
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "locked", nullable = false)
    private boolean locked = true;

    @Column(name = "verify_token")
    private String verifyToken;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt = LocalDate.now();
}
