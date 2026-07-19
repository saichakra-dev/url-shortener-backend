package com.urlshortener.dto.response;

import com.urlshortener.domain.enums.UrlStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record UrlResponse(
        String id,
        String shortCode,
        String shortUrl,
        String originalUrl,
        String customAlias,
        String createdBy,
        Instant createdAt,
        Instant expiresAt,
        long clickCount,
        UrlStatus status
) {}