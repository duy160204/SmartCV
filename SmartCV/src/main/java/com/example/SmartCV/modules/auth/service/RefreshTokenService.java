package com.example.SmartCV.modules.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.auth.domain.RefreshToken;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // Thời gian sống của refresh token: 7 ngày
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;

    /**
     * Tạo refresh token mới cho user
     */
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUserId(user.getId());
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS));

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Kiểm tra refresh token hợp lệ
     */
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    /**
     * Thu hồi token (dùng cho logout)
     */
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Thu hồi tất cả token của user (khi đổi mật khẩu)
     */
    public void revokeTokensByUser(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Tạo token mới (token rotation)
     */
    public RefreshToken rotateToken(RefreshToken oldToken) {
        // Thu hồi token cũ để tránh replay attack
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Tạo token mới
        RefreshToken newToken = new RefreshToken();
        newToken.setUserId(oldToken.getUserId());
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS));

        return refreshTokenRepository.save(newToken);
    }
}
