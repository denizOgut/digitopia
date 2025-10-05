package com.digitopia.user.infrastructure.security;

import com.digitopia.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(UUID userId, String email, Role role) {
        var now = new Date();
        var expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .claim("role", role.name())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public UUID getUserIdFromToken(String token) {
        var claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }
}
