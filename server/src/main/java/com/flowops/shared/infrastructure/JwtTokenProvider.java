package com.flowops.shared.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${flowops.jwt.secret}") String secret,
            @Value("${flowops.jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs
    ) {
        if (accessTokenExpirationMs <= 0) throw new IllegalArgumentException("Access token TTL must be positive");
        try {
            Math.addExact(System.currentTimeMillis(), accessTokenExpirationMs);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Access token TTL is too large", ex);
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateAccessToken(Long userId, UUID sessionId, Long authVersion, Instant sessionExpiry) {
        Date now = new Date();
        Date expiryDate = Date.from(sessionExpiry.isBefore(now.toInstant().plusMillis(accessTokenExpirationMs))
                ? sessionExpiry : now.toInstant().plusMillis(accessTokenExpirationMs));

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("sid", sessionId.toString())
                .claim("authVersion", authVersion)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
