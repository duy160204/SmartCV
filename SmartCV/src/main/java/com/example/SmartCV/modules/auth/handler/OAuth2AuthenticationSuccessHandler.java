package com.example.SmartCV.modules.auth.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.domain.RefreshToken;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.service.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Value("${app.security.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email"); // Standard attribute

        log.info("OAuth2 Login Success for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 execution"));

        String accessToken = jwtUtils.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // ===== Access Token Cookie =====
        response.addHeader("Set-Cookie",
                ResponseCookie.from("jwt", accessToken)
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .path("/")
                        .maxAge(86400)
                        .sameSite("Lax") // Match previous logic
                        .build()
                        .toString());

        // ===== Refresh Token Cookie =====
        response.addHeader("Set-Cookie",
                ResponseCookie.from("refresh_token", refreshToken.getToken())
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .path("/")
                        .maxAge(604800)
                        .sameSite("Lax")
                        .build()
                        .toString());

        // Determine Provider from request URI if possible, or just default.
        // Request URI: /login/oauth2/code/{registrationId} or
        // /api/login/oauth2/code/{registrationId}
        // Extract registrationId to send back to frontend.
        String uri = request.getRequestURI();
        String provider = "google"; // default
        if (uri.contains("/")) {
            String[] parts = uri.split("/");
            provider = parts[parts.length - 1];
        }

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth/callback/" + provider)
                .queryParam("status", "success")
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
