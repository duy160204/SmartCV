package com.example.SmartCV.modules.auth.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "oauth_accounts")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OAuthAccount {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String provider; 

    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String providerUserId;

    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();
}
