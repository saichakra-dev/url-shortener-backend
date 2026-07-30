package com.urlshortener.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Inject @Value fields manually in tests
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "3d7b9f2a8e1c4d6f0a5b2e8f1d4c7a9b3e6f2d5a8c1b4e7f0d3a6b9c2e5f8a1d");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs",
                86400000L);

        userDetails = User.withUsername("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void shouldGenerateToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("Should extract correct username from token")
    void shouldExtractUsername() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should validate token successfully for correct user")
    void shouldValidateToken_forCorrectUser() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject token for different user")
    void shouldRejectToken_forDifferentUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetails differentUser = User.withUsername("other@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertThat(isValid).isFalse();
    }
}