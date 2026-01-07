package com.example.SmartCV.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthLoginResponse {

    private Long userId;
    private String email;
    private String username;
    private String role;
    private String accessToken;
    private String refreshToken;

    public AuthLoginResponse(Long userId, String email, String username, String role,
                             String accessToken, String refreshToken) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getter
}
