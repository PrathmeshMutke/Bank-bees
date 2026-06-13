package com.banking.auth.service;

import com.banking.auth.entity.Role;
import com.banking.auth.entity.User;
import com.banking.auth.repository.RoleRepository;
import com.banking.auth.repository.UserRepository;
import com.banking.auth.security.JwtService;
import com.banking.core.dto.ApiResponse;
import com.banking.core.exception.CustomExceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 10;
    private static final String OTP_PREFIX = "OTP_";

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public ApiResponse<String> register(SignupRequest request) {
        log.info("[AuthService] Registering new user: username={}, email={}, mfa={}", request.getUsername(), request.getEmail(), request.isEnableMfa());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("[AuthService] Registration failed - username already exists: {}", request.getUsername());
            throw new InvalidTransactionException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("[AuthService] Registration failed - email already exists: {}", request.getEmail());
            throw new InvalidTransactionException("Email already exists");
        }
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            log.warn("[AuthService] Registration failed - phone already exists: {}", request.getPhoneNumber());
            throw new InvalidTransactionException("Phone number already exists");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("CUSTOMER").description("Standard retail customer").build()));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .status("ACTIVE")
                .failedLoginAttempts(0)
                .mfaEnabled(request.isEnableMfa() ? 1 : 0)
                .roles(Set.of(customerRole))
                .build();

        userRepository.save(user);
        log.info("[AuthService] User registered successfully: username={}", request.getUsername());
        return ApiResponse.success("Registration successful");
    }

    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        log.info("[AuthService] Login attempt for username={}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("[AuthService] Login failed - user not found: {}", request.getUsername());
                    return new ResourceNotFoundException("User not found");
                });

        // Check if locked
        if (isAccountLocked(user)) {
            log.warn("[AuthService] Login blocked - account locked for username={}, lockedUntil={}", request.getUsername(), user.getLockedUntil());
            throw new UnauthorizedException("Account is locked. Try again later.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            incrementFailedAttempts(user);
            log.warn("[AuthService] Login failed - invalid password for username={}, failedAttempts={}", request.getUsername(), user.getFailedLoginAttempts());
            throw new UnauthorizedException("Invalid username or password");
        }

        // Reset failed login attempts on success
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        log.info("[AuthService] Password verified for username={}", request.getUsername());

        // Check MFA
        if (user.getMfaEnabled() == 1) {
            String otp = generateOtpCode();
            redisTemplate.opsForValue().set(OTP_PREFIX + user.getUsername(), otp, 5, TimeUnit.MINUTES);
            log.info("[AuthService] MFA OTP generated and cached in Redis for username={}", request.getUsername());

            LoginResponse mfaResponse = new LoginResponse();
            mfaResponse.setMfaRequired(true);
            mfaResponse.setMessage("OTP sent to your registered device. (For local test, OTP: " + otp + ")");
            return ApiResponse.success(mfaResponse, "MFA required");
        }

        // Generate Tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.info("[AuthService] JWT tokens generated for username={}, roles={}", request.getUsername(), user.getRoles().stream().map(Role::getName).toList());

        LoginResponse successResponse = new LoginResponse();
        successResponse.setMfaRequired(false);
        successResponse.setAccessToken(accessToken);
        successResponse.setRefreshToken(refreshToken);
        successResponse.setUsername(user.getUsername());
        successResponse.setRoles(user.getRoles().stream().map(Role::getName).toList());
        return ApiResponse.success(successResponse, "Login successful");
    }

    @Transactional
    public ApiResponse<LoginResponse> verifyOtp(OtpRequest request) {
        log.info("[AuthService] OTP verification for username={}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("[AuthService] OTP verify failed - user not found: {}", request.getUsername());
                    return new ResourceNotFoundException("User not found");
                });

        String cachedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + user.getUsername());
        if (cachedOtp == null || !cachedOtp.equals(request.getOtp())) {
            log.warn("[AuthService] OTP verification failed - invalid or expired OTP for username={}", request.getUsername());
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        redisTemplate.delete(OTP_PREFIX + user.getUsername());
        log.info("[AuthService] OTP verified and cleared from Redis for username={}", request.getUsername());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        LoginResponse response = new LoginResponse();
        response.setMfaRequired(false);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles().stream().map(Role::getName).toList());
        log.info("[AuthService] OTP login complete - tokens issued for username={}", request.getUsername());
        return ApiResponse.success(response, "OTP verification successful");
    }

    private boolean isAccountLocked(User user) {
        if (user.getLockedUntil() == null) {
            return false;
        }
        if (user.getLockedUntil().isBefore(LocalDateTime.now())) {
            log.info("[AuthService] Account lock expired for username={}, resetting", user.getUsername());
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
            return false;
        }
        return true;
    }

    private void incrementFailedAttempts(User user) {
        int newAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newAttempts);
        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            user.setStatus("LOCKED");
            log.warn("[AuthService] Account LOCKED for username={} after {} failed attempts", user.getUsername(), newAttempts);
        }
        userRepository.save(user);
    }

    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public static class SignupRequest {
        private String username;
        private String email;
        private String password;
        private String phoneNumber;
        private boolean enableMfa;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public boolean isEnableMfa() { return enableMfa; }
        public void setEnableMfa(boolean enableMfa) { this.enableMfa = enableMfa; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class OtpRequest {
        private String username;
        private String otp;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }

    public static class LoginResponse {
        private boolean mfaRequired;
        private String accessToken;
        private String refreshToken;
        private String username;
        private List<String> roles;
        private String message;

        public boolean isMfaRequired() { return mfaRequired; }
        public void setMfaRequired(boolean mfaRequired) { this.mfaRequired = mfaRequired; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
