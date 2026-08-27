package com.example.booking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Provider responsible for generating, parsing, and validating JSON Web Tokens (JWT).
 * <p>
 * Security & Lifecycle Architecture:
 * - This component is managed as a Spring Singleton bean.
 * - The cryptographic HMAC-SHA256 {@link SecretKey} is initialized once at startup and retained in memory
 *   for the application lifecycle to provide non-blocking, constant-time signature verification.
 * - For enterprise deployments requiring external key rotation or Hardware Security Modules (HSM),
 *   this provider can be integrated with cloud Key Management Services (KMS) or Vault.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final int MIN_KEY_LENGTH_BYTES = 32;

    private final String jwtSecret;
    private final long jwtExpirationMs;

    /**
     * In-memory HMAC-SHA256 secret key for signing and verifying tokens.
     * Maintained within the singleton bean context for high-throughput stateless token operations.
     */
    private final SecretKey key;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.key = initSigningKey(jwtSecret);
    }

    /**
     * Initializes the cryptographic HMAC-SHA256 signing key from the configured JWT secret.
     * <p>
     * Dual-format handling:
     * 1. Attempts Base64 decoding if the secret is a standard Base64-encoded 256-bit key.
     * 2. Falls back to raw UTF-8 bytes if the secret is a plain passphrase / text secret.
     * <p>
     * Fails fast at application startup if the resulting key has less than 256 bits (32 bytes) of entropy.
     *
     * @param secret Configured JWT secret string
     * @return Validated {@link javax.crypto.SecretKey} for HMAC-SHA256 operations
     * @throws IllegalArgumentException if the secret is null, blank, or shorter than 32 bytes
     */
    private javax.crypto.SecretKey initSigningKey(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key must not be null or empty");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        try {
            byte[] decoded = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
            if (decoded.length >= MIN_KEY_LENGTH_BYTES) {
                keyBytes = decoded;
            } else {
                log.warn("Decoded Base64 secret length is less than {} bytes ({} bytes); falling back to raw UTF-8 string bytes", MIN_KEY_LENGTH_BYTES, decoded.length);
            }
        } catch (Exception ex) {
            log.warn("JWT secret is not valid Base64 ({}); evaluating raw UTF-8 passphrase entropy", ex.getMessage());
        }

        if (keyBytes.length < MIN_KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "JWT secret key must be at least 256 bits (" + MIN_KEY_LENGTH_BYTES + " bytes) long to ensure HMAC-SHA256 cryptographic strength. Current length: "
                            + keyBytes.length + " bytes. Please provide a stronger secret key."
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(principal);
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("userId", principal.getId())
                .claim("role", principal.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        Number userId = claims.get("userId", Number.class);
        return userId != null ? userId.longValue() : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public long getExpirationInSeconds() {
        return jwtExpirationMs / 1000;
    }
}
