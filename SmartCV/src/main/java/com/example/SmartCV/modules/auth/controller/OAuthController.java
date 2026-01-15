package com.example.SmartCV.modules.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.service.OAuthService;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth/oauth")
public class OAuthController {

    @Autowired
    private OAuthService oauthService;

    @Value("${app.security.cookie-secure}")
    private boolean cookieSecure;

    // =================== CALLBACK =================== //
    @GetMapping("/{provider}/callback")
    public ResponseEntity<?> oauthCallback(
            @PathVariable String provider,
            @RequestParam String code,
            HttpServletResponse response) {
        try {
            AuthResponseDTO auth;
            switch (provider.toLowerCase()) {
                case "google":
                    auth = oauthService.loginWithGoogle(code);
                    break;
                case "github":
                    auth = oauthService.loginWithGitHub(code);
                    break;
                case "facebook":
                    auth = oauthService.loginWithFacebook(code);
                    break;
                case "linkedin":
                    auth = oauthService.loginWithLinkedIn(code);
                    break;
                case "zalo":
                    auth = oauthService.loginWithZalo(code);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
            }

            // =================== TẠO COOKIE JWT =================== //
            ResponseCookie cookie = ResponseCookie.from("jwt", auth.getAccessToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(24 * 60 * 60) // 1 ngày
                    .sameSite("Strict")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());

            // =================== TRẢ RESPONSE JSON =================== //
            // Chỉ trả refreshToken và các thông tin cần thiết cho frontend
            AuthResponseDTO responseBody = new AuthResponseDTO(
                    auth.getEmail(),
                    auth.getName(),
                    auth.getProvider(),
                    auth.isVerified(),
                    auth.getRole(),
                    null, // Không trả lại accessToken trong body
                    auth.getRefreshToken() // Trả refreshToken
            );

            return ResponseEntity.ok(responseBody);

        } catch (OAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("OAuth login failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }
}
