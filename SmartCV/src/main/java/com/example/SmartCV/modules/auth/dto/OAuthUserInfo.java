package com.example.SmartCV.modules.auth.dto;

public record OAuthUserInfo(
        String providerUserId,
        String email,
        boolean emailVerified) {
}
