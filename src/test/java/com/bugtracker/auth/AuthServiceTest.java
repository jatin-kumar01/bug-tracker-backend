package com.bugtracker.auth;

import com.bugtracker.auth.dto.AuthResponse;
import com.bugtracker.auth.dto.RegisterRequest;
import com.bugtracker.auth.entity.RefreshToken;
import com.bugtracker.auth.repository.RefreshTokenRepository;
import com.bugtracker.auth.service.AuthService;
import com.bugtracker.exception.DuplicateResourceException;
import com.bugtracker.security.JwtService;
import com.bugtracker.user.entity.Role;
import com.bugtracker.user.entity.User;
import com.bugtracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void register_firstUser_getsAdminRoleAndTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Jatin Kumar");
        request.setEmail("jatin@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByEmailIgnoreCase("jatin@example.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("mock-jwt-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
        assertEquals(Role.ADMIN, response.getUser().getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Duplicate User");
        request.setEmail("dup@example.com");
        request.setPassword("secret123");

        when(userRepository.existsByEmailIgnoreCase("dup@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
