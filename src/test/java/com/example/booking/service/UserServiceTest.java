package com.example.booking.service;

import com.example.booking.entity.User;
import com.example.booking.enums.Role;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.UserNotFoundException;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
        testUser = new User("alice", "alice@example.com", "encodedPass", Role.USER);
        testUser.setId(10L);
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        User user = userService.getUserById(10L);
        assertNotNull(user);
        assertEquals("alice", user.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void getUserByUsername_Success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        User user = userService.getUserByUsername("alice");
        assertNotNull(user);
        assertEquals(10L, user.getId());
    }

    @Test
    void getUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("unknown"));
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User created = userService.createUser("alice", "alice@example.com", "pass123", Role.USER);
        assertNotNull(created);
        assertEquals("alice", created.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateUsername_ThrowsBadRequestException() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> userService.createUser("alice", "alice@example.com", "pass", Role.USER));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsBadRequestException() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> userService.createUser("alice", "alice@example.com", "pass", Role.USER));
    }

    @Test
    void existsByUsernameAndEmail() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertTrue(userService.existsByUsername("alice"));
        assertTrue(userService.existsByEmail("alice@example.com"));
    }
}