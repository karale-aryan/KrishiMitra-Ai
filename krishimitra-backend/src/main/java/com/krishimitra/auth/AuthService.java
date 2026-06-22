package com.krishimitra.auth;

import com.krishimitra.auth.dto.AuthResponse;
import com.krishimitra.auth.dto.LoginRequest;
import com.krishimitra.auth.dto.RegisterRequest;
import com.krishimitra.auth.internal.JwtTokenProvider;
import com.krishimitra.auth.internal.Role;
import com.krishimitra.auth.internal.UserEntity;
import com.krishimitra.auth.internal.UserRepository;
import com.krishimitra.shared.exception.BadRequestException;
import com.krishimitra.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Service handling user registration, login, and token refresh.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user.
     * Validates phone number uniqueness and optional email uniqueness,
     * hashes the password, saves the user, and returns JWT tokens.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered");
        }

        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        String preferredLanguage = StringUtils.hasText(request.getPreferredLanguage())
                ? request.getPreferredLanguage()
                : "en";

        UserEntity user = UserEntity.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.FARMER)
                .preferredLanguage(preferredLanguage)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with phone: {}", request.getPhoneNumber());

        return generateAuthResponse(user);
    }

    /**
     * Authenticate a user with phone number and password.
     * Returns JWT tokens on success.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new BadRequestException("Invalid phone number or password"));

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid phone number or password");
        }

        log.info("User logged in successfully with phone: {}", request.getPhoneNumber());
        return generateAuthResponse(user);
    }

    /**
     * Refresh the authentication tokens using a valid refresh token.
     * Validates the refresh token, extracts the user, and generates a new token pair.
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated. Please contact support.");
        }

        log.info("Tokens refreshed for user: {}", user.getPhoneNumber());
        return generateAuthResponse(user);
    }

    /**
     * Generate an AuthResponse containing access and refresh tokens for the given user.
     */
    private AuthResponse generateAuthResponse(UserEntity user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .build();
    }
}
