package com.urlshortener.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.ApiResponse;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.service.UrlService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor                    // generates constructor for urlService
public class UrlController {

    private final UrlService urlService;    // lowercase u — Java convention

    // ── Create ────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<UrlResponse>> createUrl(
            @Valid @RequestBody CreateUrlRequest request) {

        String userId = getCurrentUserId();
        log.info("Create URL request from user: {}", userId);

        UrlResponse response = urlService.createUrl(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "URL created successfully"));
    }

    // ── List My URLs ──────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<UrlResponse>>> getUserUrls() {

        String userId = getCurrentUserId();
        log.info("Get URLs request from user: {}", userId);

        List<UrlResponse> urls = urlService.getUserUrls(userId);
        return ResponseEntity.ok(
                ApiResponse.success(urls, "URLs retrieved successfully"));
    }

    // ── Get Single URL ────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UrlResponse>> getUrlById(
            @PathVariable String id) {

        String userId = getCurrentUserId();
        UrlResponse response = urlService.getUrlById(id, userId);
        return ResponseEntity.ok(
                ApiResponse.success(response, "URL retrieved successfully"));
    }

    // ── Delete ────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String id) {

        String userId = getCurrentUserId();
        log.info("Delete URL {} by user: {}", id, userId);

        urlService.deleteUrl(id, userId);
        return ResponseEntity.noContent().build();  // 204 — no body on delete
    }

    // ── Helper ────────────────────────────────────────────────────
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}