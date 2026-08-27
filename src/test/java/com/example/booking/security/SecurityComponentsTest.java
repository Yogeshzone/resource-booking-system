package com.example.booking.security;

import com.example.booking.entity.User;
import com.example.booking.enums.Role;
import com.example.booking.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityComponentsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private CustomUserDetailsService userDetailsService;
    private CustomAccessDeniedHandler accessDeniedHandler;
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(userRepository);
        accessDeniedHandler = new CustomAccessDeniedHandler();
        authenticationEntryPoint = new JwtAuthenticationEntryPoint();
    }

    @Test
    void customUserDetailsService_Success() {
        User user = new User("john", "john@example.com", "pass", Role.ADMIN);
        user.setId(10L);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("john");
        assertNotNull(details);
        assertEquals("john", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void customUserDetailsService_NotFound_ThrowsUsernameNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown"));
    }

    @Test
    void customAccessDeniedHandler_WritesJsonResponse() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestURI()).thenReturn("/admin/secret");

        accessDeniedHandler.handle(request, response, new AccessDeniedException("Access Denied"));

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        String output = stringWriter.toString();
        assertTrue(output.contains("403"));
        assertTrue(output.contains("Forbidden"));
    }

    @Test
    void jwtAuthenticationEntryPoint_WritesJsonResponse() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getRequestURI()).thenReturn("/reservations");

        authenticationEntryPoint.commence(request, response, new org.springframework.security.authentication.BadCredentialsException("Bad token"));

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        String output = stringWriter.toString();
        assertTrue(output.contains("401"));
        assertTrue(output.contains("Unauthorized"));
    }

    @Test
    void userPrincipal_Methods() {
        UserPrincipal principal = new UserPrincipal(
                1L, "user1", "user1@example.com", "pass", Role.USER,
                java.util.Collections.emptyList()
        );
        assertEquals(1L, principal.getId());
        assertEquals("user1@example.com", principal.getEmail());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertTrue(principal.isEnabled());
        assertFalse(principal.isAdmin());
        assertEquals(principal, new UserPrincipal(1L, "user1", "user1@example.com", "pass", Role.USER, java.util.Collections.emptyList()));
        assertEquals(principal.hashCode(), new UserPrincipal(1L, "user1", "user1@example.com", "pass", Role.USER, java.util.Collections.emptyList()).hashCode());
    }
}