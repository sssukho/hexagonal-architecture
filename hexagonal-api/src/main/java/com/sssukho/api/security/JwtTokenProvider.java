package com.sssukho.api.security;

import com.sssukho.api.config.properties.SecurityConfigurationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationInMs;
    private final long refreshTokenExpirationInMs;

    public JwtTokenProvider(SecurityConfigurationProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes());
        this.accessTokenExpirationInMs = properties.jwt().accessTokenExpirationMs();
        this.refreshTokenExpirationInMs = properties.jwt().refreshTokenExpirationMs();
    }

    public String generateAccessToken(String usernameFromUserDetails) {
        Date expiryDate = new Date(System.currentTimeMillis() + accessTokenExpirationInMs);
        return Jwts.builder()
            .subject(usernameFromUserDetails)
            .issuedAt(new Date())
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }

    public String generateRefreshToken(String usernameFromUserDetails) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshTokenExpirationInMs);
        return Jwts.builder()
            .subject(usernameFromUserDetails)
            .issuedAt(new Date())
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }

    public String extractMemberNameFromToken(String token) {
        JwtParser parser = Jwts.parser().verifyWith(key).build();
        Jws<Claims> claimsJws = parser.parseSignedClaims(token);
        return claimsJws.getPayload().getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            JwtParser parser = Jwts.parser().verifyWith(key).build();
            parser.parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Failed to validate token", e);
            return false;
        }
    }
}

