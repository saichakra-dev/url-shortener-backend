package com.urlshortener.service.impl;

import com.urlshortener.domain.entity.User;
import com.urlshortener.domain.enums.Role;
import com.urlshortener.dto.request.LoginRequest;
import com.urlshortener.dto.request.RegisterRequest;
import com.urlshortener.dto.response.AuthResponse;
import com.urlshortener.exception.DuplicateResourceException;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Test User",
                "test@example.com",
                "password123"
        );

        loginRequest = new LoginRequest(
                "test@example.com",
                "password123"
        );

        savedUser = User.builder()
                .id("64abc")
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("Should register user successfully and return token")
    void shouldRegisterUser_successfully() {
        // Arrange
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email exists")
    void shouldThrow_whenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(any())).thenReturn(true);

        // Assert + Act
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already");
    }

    @Test
    @DisplayName("Should login successfully and return token")
    void shouldLoginUser_successfully() {
        // Arrange
        when(authenticationManager.authenticate(any()))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on wrong password")
    void shouldThrow_whenPasswordIsWrong() {
        // Arrange
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Assert + Act
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}