package com.bugtracker.auth.service;

import com.bugtracker.auth.dto.AuthResponse;
import com.bugtracker.auth.dto.LoginRequest;
import com.bugtracker.auth.dto.RefreshTokenRequest;
import com.bugtracker.auth.dto.RegisterRequest;
import com.bugtracker.auth.entity.RefreshToken;
import com.bugtracker.auth.repository.RefreshTokenRepository;
import com.bugtracker.exception.DuplicateResourceException;
import com.bugtracker.exception.TokenRefreshException;
import com.bugtracker.security.JwtService;
import com.bugtracker.user.dto.UserResponse;
import com.bugtracker.user.entity.Role;
import com.bugtracker.user.entity.User;
import com.bugtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }


        Role assignedRole = userRepository.count() == 0 ? Role.ADMIN : Role.DEVELOPER;

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new TokenRefreshException("User not found."));

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token."));

        if (existingToken.isRevoked()) {
            throw new TokenRefreshException("Refresh token has been revoked. Please log in again.");
        }

        if (existingToken.getExpiryDate().isBefore(Instant.now())) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new TokenRefreshException("Refresh token expired. Please log in again.");
        }

        User user = existingToken.getUser();
        if (!user.isActive()) {
            throw new TokenRefreshException("User account is deactivated.");
        }

        // Rotate refresh token
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID() + "-" + UUID.randomUUID())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresInMs(jwtService.getAccessTokenExpirationMs())
                .user(UserResponse.from(user))
                .build();
    }
}
