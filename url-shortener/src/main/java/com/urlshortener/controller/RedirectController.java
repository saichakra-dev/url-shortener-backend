package com.urlshortener.controller;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.kafka.producer.ClickEventMessage;
import com.urlshortener.kafka.producer.ClickEventProducer;
import com.urlshortener.service.UrlService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;
    private final ClickEventProducer clickEventProducer;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        log.info("Redirect request for shortCode: {}", shortCode);

        // Resolve URL (fast — only reads from DB or cache later)
        String originalUrl = urlService.resolveShortCode(shortCode);

        // Publish click event asynchronously — non-blocking
        publishClickEvent(shortCode, request);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    private void publishClickEvent(String shortCode,
                                   HttpServletRequest request) {
        try {
            ClickEventMessage message = ClickEventMessage.builder()
                    .shortCode(shortCode)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .referer(request.getHeader("Referer"))
                    .clickedAt(Instant.now())
                    .build();

            clickEventProducer.publishClickEvent(message);
        } catch (Exception e) {
            // Never let analytics failure break the redirect
            log.error("Failed to publish click event: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}