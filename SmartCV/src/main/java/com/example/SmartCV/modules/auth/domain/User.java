package com.example.SmartCV.modules.auth.domain;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String username;
    private String password;
    private String avatarURL;

    @Column(name = "role_id")
    private Long roleId;

    // ===== QUAN TRỌNG =====
    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean locked = true;

    @Column(name = "verify_token")
    private String verifyToken;

    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "updated_at")
    private LocalDate updatedAt = LocalDate.now();
}
