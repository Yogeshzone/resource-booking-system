package com.example.booking.service;

import com.example.booking.dto.auth.LoginRequest;
import com.example.booking.dto.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
