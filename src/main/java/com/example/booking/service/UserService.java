package com.example.booking.service;

import com.example.booking.entity.User;
import com.example.booking.enums.Role;

public interface UserService {

    User getUserById(Long id);

    User getUserByUsername(String username);

    User createUser(String username, String email, String rawPassword, Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
