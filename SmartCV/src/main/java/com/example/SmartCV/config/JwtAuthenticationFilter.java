package com.example.SmartCV.config;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.example.SmartCV.common.utils.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Collection;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.common.utils.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean skip = path.startsWith("/api/auth/") || 
               path.startsWith("/api/public/") || 
               path.startsWith("/api/payments/vnpay/") || 
               path.startsWith("/uploads/");
        
        if (skip && path.contains("/vnpay/ipn")) {
            log.error("🔥 [IPN BYPASS JWT FILTER] Path: {}", path);
        }
        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractToken(request);
        log.info("JWT = {}", token);

        if (token != null && jwtUtils.validateToken(token)) {
            // REDIS BEST EFFORT FAIL-OPEN
            boolean isRevoked = false;
            try {
                if (Boolean.TRUE.equals(redisTemplate.hasKey("jwt_blacklist:" + token))) {
                    isRevoked = true;
                }
            } catch (Exception redisEx) {
                log.warn("Redis Connection Failed! FAIL-OPEN strategy applied...");
            }

            if (!isRevoked) {
                String email = jwtUtils.getEmailFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

                log.info("AUTHENTICATED: JWT accepted for {}", email);
                log.info("AUTH SET = {}", SecurityContextHolder.getContext().getAuthentication());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Header first (PRIMARY) to prevent cross-app cookie mixing
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // 2. Fallback cookie
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
