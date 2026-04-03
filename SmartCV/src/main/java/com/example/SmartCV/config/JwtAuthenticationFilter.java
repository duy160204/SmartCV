package com.example.SmartCV.config;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.common.utils.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate; // Redis caching for blacklist

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            log.debug("JWT FILTER HIT - URI: {}", request.getRequestURI());

            String header = request.getHeader("Authorization");
            log.debug("JwtAuthenticationFilter -> Authorization header value: {}", header);

            String token = extractToken(request);
            log.debug("JwtAuthenticationFilter -> Token after extraction: {}", token);
            log.debug("JWT extracted: {}", token != null ? "YES" : "NO");

            if (token != null) {
                boolean isValid = jwtUtils.validateToken(token);
                log.debug("JwtAuthenticationFilter -> Result of validateToken(): {}", isValid);

                if (isValid) {
                    // REVOCATION / BLACKLIST CHECK
                    try {
                        if (Boolean.TRUE.equals(redisTemplate.hasKey("jwt_blacklist:" + token))) {
                            log.warn("Blocked revoked access token attempt.");
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revoked limit.");
                            return;
                        }
                    } catch (Exception redisEx) {
                        log.error("Redis Connection Failed! FAIL-OPEN strategy applied for Blacklist checking. Allowing authentication to bypass blacklist. Error: {}", redisEx.getMessage());
                    }

                    String email = jwtUtils.getEmailFromToken(token);
                    log.debug("JwtAuthenticationFilter -> Email extracted: {}", email);
                    
                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        UsernamePasswordAuthenticationToken auth = 
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        log.debug("Setting authentication for user: {}", email);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        
                        log.debug("JwtAuthenticationFilter -> Authentication is set.");
                        log.debug("JwtAuthenticationFilter -> Current SecurityContext Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
                    }

                } else {
                    log.warn("JwtAuthenticationFilter -> Token is INVALID");
                }
            }
        } catch (Exception e) {
            log.error("Authentication filter exception: {}", e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        log.debug("JWT present in cookies: {}", request.getCookies() != null);
        // 1. Cookie first (PRIMARY)
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 2. Fallback header
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
