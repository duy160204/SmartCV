package com.example.SmartCV.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.example.SmartCV.modules.auth.domain.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
@lombok.extern.slf4j.Slf4j
public class JWTUtils {

    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String secret;

    @org.springframework.beans.factory.annotation.Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private javax.crypto.SecretKey key;

    @jakarta.annotation.PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT Secret is too short or missing!");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("name", user.getUsername())
                .claim("roles", user.getRoleId())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | io.jsonwebtoken.MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
