package com.example.SmartCV.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.RoleRepository;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JWTUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:900000}") // Default 15 mins for AT
    private long jwtExpirationMs;

    // 7 days refresh
    private final long jwtRefreshExpirationMs = 604800000L; 

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleRepository roleRepository;
    private javax.crypto.SecretKey key;

    @jakarta.annotation.PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT Secret must be at least 32 characters for HS512!");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        String roleName = roleRepository.findById(user.getRoleId())
                .map(com.example.SmartCV.modules.auth.domain.Role::getName)
                .orElse("ROLE_USER");
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName.toUpperCase();
        }

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("name", user.getUsername())
                .claim("role", roleName)
                .claim("id", user.getId())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(User user) {
        long now = System.currentTimeMillis();
        String refreshToken = Jwts.builder()
                .setSubject(user.getEmail())
                .setId(UUID.randomUUID().toString()) // unique ID for revocation
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtRefreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        
        // Store in Redis mapped to user 
        try {
            redisTemplate.opsForValue().set("refresh_token:" + user.getEmail(), refreshToken, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis FAIL-OPEN: Cannot cache refresh token for {}, continuing. Error: {}", user.getEmail(), e.getMessage());
        }
        return refreshToken;
    }

    public void revokeToken(String token) throws Exception {
        long expiration = getExpirationDateFromToken(token).getTime();
        long ttl = expiration - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set("jwt_blacklist:" + token, "REVOKED", ttl, TimeUnit.MILLISECONDS);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            // Must not be blacklisted (now checked in JwtAuthenticationFilter) 
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
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public Long getIdFromToken(String token) {
        Object idVal = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("id");
        return idVal instanceof Number ? ((Number) idVal).longValue() : null;
    }

    public String getRoleFromToken(String token) {
        Object roleVal = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role");
        return roleVal != null ? roleVal.toString() : null;
    }

    public Date getExpirationDateFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();
    }
}
