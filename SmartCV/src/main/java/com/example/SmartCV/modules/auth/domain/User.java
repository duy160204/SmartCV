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

    @Column(nullable = false)
    private String password;

    private String avatarURL;

    @Column(name = "role_id")
    private Long roleId;

    // ===== CORE STATUS =====
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;  // mặc định false khi tạo mới

    @Column(name = "locked", nullable = false)
    private boolean locked = true;     // mặc định khóa tài khoản

    @Column(name = "verify_token")
    private String verifyToken;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt = LocalDate.now();
}
