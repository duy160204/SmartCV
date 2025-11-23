package com.example.SmartCV.modules.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.OAuthRequestDTO;
import com.example.SmartCV.modules.auth.service.OAuthService;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth/oauth")
public class OAuthController {

    @Autowired
    private OAuthService oauthService;

    @Autowired
    private JWTUtils jwtUtils;

    // ============================= GOOGLE ============================= //
    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody OAuthRequestDTO request, HttpServletResponse response) {
        return handleOAuthLogin(request.getToken(), "google", response);
    }

    // ============================= GITHUB ============================= //
    @PostMapping("/github")
    public ResponseEntity<?> loginGitHub(@RequestBody OAuthRequestDTO request, HttpServletResponse response) {
        return handleOAuthLogin(request.getToken(), "github", response);
    }

    // ============================= FACEBOOK ============================= //
    @PostMapping("/facebook")
    public ResponseEntity<?> loginFacebook(@RequestBody OAuthRequestDTO request, HttpServletResponse response) {
        return handleOAuthLogin(request.getToken(), "facebook", response);
    }

    // ============================= LINKEDIN ============================= //
    @PostMapping("/linkedin")
    public ResponseEntity<?> loginLinkedIn(@RequestBody OAuthRequestDTO request, HttpServletResponse response) {
        return handleOAuthLogin(request.getToken(), "linkedin", response);
    }

    // ============================= ZALO ============================= //
    @PostMapping("/zalo")
    public ResponseEntity<?> loginZalo(@RequestBody OAuthRequestDTO request, HttpServletResponse response) {
        return handleOAuthLogin(request.getToken(), "zalo", response);
    }

    // ============================= COMMON LOGIC ============================= //
    private ResponseEntity<?> handleOAuthLogin(String tokenValue, String provider, HttpServletResponse response) {
        try {
            AuthResponseDTO auth;

            switch (provider.toLowerCase()) {
                case "google":
                    auth = oauthService.loginWithGoogle(tokenValue);
                    break;
                case "github":
                    auth = oauthService.loginWithGitHub(tokenValue);
                    break;
                case "facebook":
                    auth = oauthService.loginWithFacebook(tokenValue);
                    break;
                case "linkedin":
                    auth = oauthService.loginWithLinkedIn(tokenValue);
                    break;
                case "zalo":
                    auth = oauthService.loginWithZalo(tokenValue);
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unsupported OAuth provider: " + provider);
            }

            // Tạo cookie JWT
            ResponseCookie cookie = ResponseCookie.from("jwt", auth.getToken())
                    .httpOnly(true)
                    .secure(true) // bật nếu HTTPS
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return ResponseEntity.ok(auth);

        } catch (OAuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
