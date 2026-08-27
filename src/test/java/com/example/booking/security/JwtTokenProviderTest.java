package com.example.booking.security;

import com.example.booking.enums.Role;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String STRONG_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private JwtTokenProvider jwtTokenProvider;
    private UserPrincipal testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(STRONG_SECRET, 3600000);
        testUser = new UserPrincipal(
                1L,
                "testuser",
                "test@example.com",
                "secret",
                Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generateAndValidateToken_Success() {
        String token = jwtTokenProvider.generateToken(testUser);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
        assertEquals(1L, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("USER", jwtTokenProvider.getRoleFromToken(token));
        assertEquals(3600L, jwtTokenProvider.getExpirationInSeconds());
    }

    @Test
    void generateToken_FromAuthentication() {
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        testUser, null, testUser.getAuthorities()
                );
        String token = jwtTokenProvider.generateToken(auth);
        assertNotNull(token);
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalseAndThrowsOnGetClaims() {
        // Create a provider with -1000ms expiration (already expired token)
        JwtTokenProvider expiredProvider = new JwtTokenProvider(STRONG_SECRET, -1000L);
        String expiredToken = expiredProvider.generateToken(testUser);

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtTokenProvider.getClaims(expiredToken));
    }

    @Test
    void validateToken_MalformedToken_ReturnsFalseAndThrowsOnGetClaims() {
        assertFalse(jwtTokenProvider.validateToken("not.a.valid.jwt.token"));
        assertThrows(io.jsonwebtoken.JwtException.class, () -> jwtTokenProvider.getClaims("not.a.valid.jwt.token"));
    }

    @Test
    void validateToken_NullOrEmptyToken_ReturnsFalseAndThrowsOnGetClaims() {
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken("   "));
        assertFalse(jwtTokenProvider.validateToken(null));
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.getClaims(""));
    }

    @Test
    void constructor_RawPassphraseKey_Success() {
        String rawPassphrase = "ThisIsA32ByteLongSecurePassphraseForTesting!";
        JwtTokenProvider rawProvider = new JwtTokenProvider(rawPassphrase, 3600000);
        String token = rawProvider.generateToken(testUser);
        assertTrue(rawProvider.validateToken(token));
        assertEquals("testuser", rawProvider.getUsernameFromToken(token));
    }

    @Test
    void constructor_WeakSecret_ThrowsIllegalArgumentException() {
        String weakSecret = "short-key";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtTokenProvider(weakSecret, 3600000)
        );
        assertTrue(ex.getMessage().contains("at least 256 bits"));
    }

    @Test
    void constructor_NullOrEmptySecret_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider(null, 3600000));
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider("   ", 3600000));
    }
}