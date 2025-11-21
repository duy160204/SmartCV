package com.example.SmartCV.modules.auth.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String username;
    private String password;
    private String avatarURL;

    @Column(name="role_id")
    private Long roleId;

    @Column(nullable = false)
    private boolean isVerified=false;

    @Column(name = "verify_token")
    private String verifyToken;

    @Column(name = "created_at")
    private LocalDate created_at = LocalDate.now();

    @Column(name = "updated_at")
    private LocalDate updated_at = LocalDate.now();



}
