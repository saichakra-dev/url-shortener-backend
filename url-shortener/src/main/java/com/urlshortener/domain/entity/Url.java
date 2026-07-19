package com.urlshortener.domain.entity;

import java.time.Instant;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.urlshortener.domain.enums.UrlStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "urls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    private String id;

    @Indexed(unique = true)
    private String shortCode;

    private String originalUrl;

    // Nullable
    private String customAlias;

    @Indexed
    private String createdBy;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Indexed(expireAfter = "0s")
    private Instant expiresAt;

    @Builder.Default
    private long clickCount = 0L;

    @Builder.Default
    private UrlStatus status = UrlStatus.ACTIVE;
}