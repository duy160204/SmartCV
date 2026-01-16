package com.example.SmartCV.modules.auth.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
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

    @Value("${app.security.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // =========================================================
    // 1️⃣ START OAUTH FLOW
    // =========================================================
    @GetMapping("/{provider}")
    public void oauthStart(
            @PathVariable String provider,
            HttpServletResponse response
    ) throws IOException {

        String state = UUID.randomUUID().toString();

        ResponseCookie stateCookie = ResponseCookie.from("oauth_state", state)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(300)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", stateCookie.toString());

        String authUrl;
        switch (provider.toLowerCase()) {
            case "google" -> authUrl = oauthService.getGoogleAuthorizationUrl(state);
            case "github" -> authUrl = oauthService.getGithubAuthorizationUrl(state);
            case "facebook" -> authUrl = oauthService.getFacebookAuthorizationUrl(state);
            case "linkedin" -> authUrl = oauthService.getLinkedInAuthorizationUrl(state);
            case "zalo" -> authUrl = oauthService.getZaloAuthorizationUrl(state);
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        response.sendRedirect(authUrl);
    }

    // =========================================================
    // 2️⃣ OAUTH CALLBACK
    // =========================================================
    @GetMapping("/{provider}/callback")
    public void oauthCallback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @CookieValue(name = "oauth_state", required = false) String savedState,
            HttpServletResponse response
    ) throws IOException {

        try {
            if (code == null) {
                throw new OAuthException("Missing authorization code");
            }

            if (savedState == null || !savedState.equals(state)) {
                throw new OAuthException("Invalid OAuth state");
            }

            AuthResponseDTO auth;
            switch (provider.toLowerCase()) {
                case "google" -> auth = oauthService.loginWithGoogle(code);
                case "github" -> auth = oauthService.loginWithGitHub(code);
                case "facebook" -> auth = oauthService.loginWithFacebook(code);
                case "linkedin" -> auth = oauthService.loginWithLinkedIn(code);
                case "zalo" -> auth = oauthService.loginWithZalo(code);
                default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
            }

            // ===== Access Token =====
            response.addHeader("Set-Cookie",
                    ResponseCookie.from("jwt", auth.getAccessToken())
                            .httpOnly(true)
                            .secure(cookieSecure)
                            .path("/")
                            .maxAge(86400)
                            .sameSite("Lax")
                            .build()
                            .toString()
            );

            // ===== Refresh Token =====
            response.addHeader("Set-Cookie",
                    ResponseCookie.from("refresh_token", auth.getRefreshToken())
                            .httpOnly(true)
                            .secure(cookieSecure)
                            .path("/")
                            .maxAge(604800)
                            .sameSite("Lax")
                            .build()
                            .toString()
            );

            // ===== Clear state =====
            response.addHeader("Set-Cookie",
                    ResponseCookie.from("oauth_state", "")
                            .path("/")
                            .maxAge(0)
                            .build()
                            .toString()
            );

            response.sendRedirect(frontendUrl + "/oauth/callback/" + provider + "?status=success");

        } catch (Exception e) {
            response.sendRedirect(
                    frontendUrl + "/oauth/callback/" + provider
                            + "?status=error&message=" + e.getMessage()
            );
        }
    }
}
