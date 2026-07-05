package com.urlshortener.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        String type,
        String email,
        String role
) {
    public static AuthResponse of(String token, String email, String role) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(email)
                .role(role)
                .build();
    }
}
