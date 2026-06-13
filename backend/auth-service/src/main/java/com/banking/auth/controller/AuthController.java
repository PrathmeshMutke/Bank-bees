package com.banking.auth.controller;

import com.banking.auth.service.AuthService;
import com.banking.auth.service.AuthService.LoginRequest;
import com.banking.auth.service.AuthService.LoginResponse;
import com.banking.auth.service.AuthService.OtpRequest;
import com.banking.auth.service.AuthService.SignupRequest;
import com.banking.core.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody SignupRequest request) {
        log.info("POST /api/v1/auth/register - Registration attempt for username={}", request.getUsername());
        ResponseEntity<ApiResponse<String>> response = ResponseEntity.ok(authService.register(request));
        log.info("Registration successful for username={}", request.getUsername());
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - Login attempt for username={}", request.getUsername());
        ResponseEntity<ApiResponse<LoginResponse>> response = ResponseEntity.ok(authService.login(request));
        log.info("Login request processed for username={}", request.getUsername());
        return response;
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(@RequestBody OtpRequest request) {
        log.info("POST /api/v1/auth/verify-otp - OTP verification attempt for username={}", request.getUsername());
        ResponseEntity<ApiResponse<LoginResponse>> response = ResponseEntity.ok(authService.verifyOtp(request));
        log.info("OTP verification processed for username={}", request.getUsername());
        return response;
    }
}
