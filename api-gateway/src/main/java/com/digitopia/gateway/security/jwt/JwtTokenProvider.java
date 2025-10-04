package com.digitopia.gateway.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public boolean isTokenExpired(Claims claims) {
        var expiration = claims.getExpiration();
        if (expiration == null) {
            Long expSeconds = claims.get("exp", Long.class);
            if (expSeconds != null) {
                expiration = new Date(expSeconds * 1000);
            }
        }
        return expiration.before(new Date());
    }

    public String getUserId(Claims claims) {
        return claims.getSubject();
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public String getEmail(Claims claims) {
        return claims.get("email", String.class);
    }
}