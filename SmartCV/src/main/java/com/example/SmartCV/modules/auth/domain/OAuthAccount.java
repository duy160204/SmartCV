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
@Table(name = "oauth_accounts")
@Getter
@Setter
public class OAuthAccount {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String provider; 

    private String providerUserId;

    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();
}
