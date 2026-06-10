package com.aimr.notify.security.util;

import com.aimr.notify.config.ApplicationProperties;
import com.aimr.notify.models.dto.response.AuthenticatedUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final ApplicationProperties properties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getJwtSecret()));
    }

    public String generateToken(String id, String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .claim("userId", id)
                .expiration(new Date(System.currentTimeMillis() + properties.getJwtExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token){
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public boolean isTokenValid(String token, AuthenticatedUserDetails authenticatedUserDetails) {
        String username = extractUsername(token);
        String userId = extractUserId(token);
        return username.equals(authenticatedUserDetails.getUsername())
                && userId.equals(authenticatedUserDetails.getUserId())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}