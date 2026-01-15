package com.example.SmartCV.modules.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.service.OAuthService;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/auth/oauth")
public class OAuthController {

    @Autowired
    private OAuthService oauthService;

    @Value("${app.security.cookie-secure}")
    private boolean cookieSecure;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // =================== CALLBACK =================== //
    @GetMapping("/{provider}/callback")
    public void oauthCallback(
            @PathVariable String provider,
            @RequestParam String code,
            HttpServletResponse response) throws IOException {
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

            // 1. Set Access Token Cookie
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", auth.getAccessToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(24 * 60 * 60) // 1 day
                    .sameSite("Strict")
                    .build();
            response.addHeader("Set-Cookie", jwtCookie.toString());

            // 2. Set Refresh Token Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", auth.getRefreshToken())
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 days (example)
                    .sameSite("Strict")
                    .build();
            response.addHeader("Set-Cookie", refreshCookie.toString());

            // 3. Safe Redirect to Frontend
            String redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl + "/auth/callback/" + provider)
                    .queryParam("status", "success")
                    .build().toUriString();

            response.sendRedirect(redirectUrl);

        } catch (OAuthException e) {
            String redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl + "/auth/callback/" + provider)
                    .queryParam("status", "error")
                    .queryParam("message", "OAuth Login Failed") // Generic message for security
                    .build().toUriString();
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            String redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendUrl + "/auth/callback/" + provider)
                    .queryParam("status", "error")
                    .queryParam("message", "Internal Server Error")
                    .build().toUriString();
            response.sendRedirect(redirectUrl);
        }
    }
}
