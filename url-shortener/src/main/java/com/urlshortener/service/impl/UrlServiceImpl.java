package com.urlshortener.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.urlshortener.domain.entity.Url;
import com.urlshortener.domain.enums.UrlStatus;
import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.exception.DuplicateResourceException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.service.UrlService;
import com.urlshortener.util.Base62Encoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public UrlResponse createUrl(CreateUrlRequest request, String userId) {
        String alias = request.customAlias();

        // Check duplicate alias
        if (alias != null && !alias.isBlank()
                && urlRepository.existsByCustomAlias(alias)) {
            throw new DuplicateResourceException("Custom alias already taken");
        }

        // Determine shortCode
        String shortCode;
        if (alias != null && !alias.isBlank()) {
            shortCode = alias;                      // use alias as shortCode
        } else {
            do {
                shortCode = base62Encoder.generate();
            } while (urlRepository.existsByShortCode(shortCode));
        }

        // Build entity
        Url url = Url.builder()
                .shortCode(shortCode)
                .originalUrl(request.originalUrl())
                .customAlias(alias)
                .createdBy(userId)
                .expiresAt(request.expiresAt())
                .status(UrlStatus.ACTIVE)
                .build();

        Url saved = urlRepository.save(url);
        log.info("Created short URL: {} for user: {}", shortCode, userId);
        return toResponse(saved);
    }

    @Override
    public UrlResponse getUrlById(String id, String userId) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        // Return 404 even if found but belongs to another user
        if (!url.getCreatedBy().equals(userId)) {
            throw new ResourceNotFoundException("URL not found");
        }

        return toResponse(url);
    }

    @Override
    public List<UrlResponse> getUserUrls(String userId) {
        return urlRepository.findByCreatedBy(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteUrl(String id, String userId) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        if (!url.getCreatedBy().equals(userId)) {
            throw new ResourceNotFoundException("URL not found");
        }

        urlRepository.delete(url);
        log.info("Deleted URL: {} by user: {}", id, userId);
    }

    @Override
    public String resolveShortCode(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        if (url.getStatus() != UrlStatus.ACTIVE) {
            throw new ResourceNotFoundException("URL is not active");
        }

        if (url.getExpiresAt() != null
                && url.getExpiresAt().isBefore(Instant.now())) {
            throw new ResourceNotFoundException("URL has expired");
        }

        // // Increment click count
        // url.setClickCount(url.getClickCount() + 1);
        // urlRepository.save(url);

        return url.getOriginalUrl();
    }

    private UrlResponse toResponse(Url url) {
        return UrlResponse.builder()
                .id(url.getId())
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .customAlias(url.getCustomAlias())
                .createdBy(url.getCreatedBy())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .clickCount(url.getClickCount())
                .status(url.getStatus())
                .build();
    }
}