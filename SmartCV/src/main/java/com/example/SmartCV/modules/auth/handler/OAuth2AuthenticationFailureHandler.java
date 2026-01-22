package com.example.SmartCV.modules.auth.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 Login Failed. URI: {}, Error: {}", request.getRequestURI(), exception.getMessage());

        // Determine Provider from request URI if possible for context
        // Request URI: /api/login/oauth2/code/{registrationId}
        String uri = request.getRequestURI();
        String provider = "unknown";
        if (uri.contains("/")) {
            String[] parts = uri.split("/");
            provider = parts[parts.length - 1];
        }

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth/callback/" + provider)
                .queryParam("status", "error")
                .queryParam("message", exception.getMessage())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
