package com.urlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public record CreateUrlRequest(

        @NotBlank(message = "Original URL is required")
        @URL(message = "Must be a valid URL including http:// or https://")
        String originalUrl,

        @Size(min = 3, max = 30, message = "Custom alias must be between 3 and 30 characters")
        @Pattern(
            regexp = "^[a-zA-Z0-9-_]*$",
            message = "Custom alias can only contain letters, numbers, hyphens and underscores"
        )
        String customAlias,      // nullable — no @NotBlank

        Instant expiresAt        // nullable — no validation needed

) {}