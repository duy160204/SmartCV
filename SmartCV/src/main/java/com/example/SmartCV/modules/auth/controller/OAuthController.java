package com.example.SmartCV.modules.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.service.OAuthService;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth/oauth")
public class OAuthController {

    @Autowired
    private OAuthService oauthService;

    // =================== CALLBACK =================== //
    @GetMapping("/{provider}/callback")
    public void oauthCallback(@PathVariable String provider, @RequestParam String code, HttpServletResponse response) {
        try {
            AuthResponseDTO auth;
            switch (provider.toLowerCase()) {
                case "google": auth = oauthService.loginWithGoogle(code); break;
                case "github": auth = oauthService.loginWithGitHub(code); break;
                case "facebook": auth = oauthService.loginWithFacebook(code); break;
                case "linkedin": auth = oauthService.loginWithLinkedIn(code); break;
                case "zalo": auth = oauthService.loginWithZalo(code); break;
                default: throw new IllegalArgumentException("Unsupported OAuth provider");
            }

            // Tạo cookie JWT
            ResponseCookie cookie = ResponseCookie.from("jwt", auth.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            response.sendRedirect("/"); // redirect về giao diện chính sau login

        } catch (OAuthException e) {
            throw new RuntimeException("OAuth login failed: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
