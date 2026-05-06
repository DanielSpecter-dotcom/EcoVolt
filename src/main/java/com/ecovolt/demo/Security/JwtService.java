package com.ecovolt.demo.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String EMAIL_VERIFICATION_TOKEN_TYPE = "email_verification";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${security.jwt.secret:ecovolt-demo-secret-key-change-me-please-32-chars}") String secret,
            @Value("${security.jwt.expiration-ms:86400000}") long expirationMillis) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .claims(Map.of(
                        TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE,
                        "authorities",
                        userDetails.getAuthorities()
                                .stream()
                                .map(Object::toString)
                                .toList()
                ))
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expirationMillis)))
                .signWith(secretKey)
                .compact();
    }

    public String generateEmailVerificationToken(String correo, long expirationMillis) {
        return Jwts.builder()
                .claims(Map.of(TOKEN_TYPE_CLAIM, EMAIL_VERIFICATION_TOKEN_TYPE))
                .subject(correo)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expirationMillis)))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && ACCESS_TOKEN_TYPE.equals(tokenType);
    }

    public boolean isEmailVerificationTokenValid(String token, String correo) {
        Claims claims = extractAllClaims(token);
        String subject = claims.getSubject();
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        Date expiration = claims.getExpiration();

        return correo.equals(subject)
                && EMAIL_VERIFICATION_TOKEN_TYPE.equals(tokenType)
                && expiration.after(new Date());
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
