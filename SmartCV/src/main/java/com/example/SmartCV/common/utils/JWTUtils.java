package com.example.SmartCV.common.utils;

import java.sql.Date;

import org.springframework.stereotype.Component;

import com.example.SmartCV.modules.auth.domain.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;

@Component
@Setter
@Getter
public class JWTUtils {
    private final String secret="DuyLvSmartCVProjectSecretKey";
    private final long jwtExpirationMs=24*60*60*1000; 

    public String generateToken(User user){
        return Jwts.builder()
                .setSubject((user.getEmail()))
                .claim("name", user.getUsername())
                .claim("roles", user.getRoleId())
                .setIssuedAt(new Date(jwtExpirationMs))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public String getEmailFromToken(String token){
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
