package com.example.booking.service.impl;

import com.example.booking.dto.auth.LoginRequest;
import com.example.booking.dto.auth.LoginResponse;
import com.example.booking.security.JwtTokenProvider;
import com.example.booking.security.UserPrincipal;
import com.example.booking.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Sets the authenticated principal in the current request thread context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String jwt = jwtTokenProvider.generateToken(authentication);
        long expiresIn = jwtTokenProvider.getExpirationInSeconds();

        log.info("User '{}' successfully authenticated with role '{}'", principal.getUsername(), principal.getRole());

        return new LoginResponse(
                jwt,
                "Bearer",
                expiresIn,
                principal.getUsername(),
                principal.getRole().name()
        );
    }
}
