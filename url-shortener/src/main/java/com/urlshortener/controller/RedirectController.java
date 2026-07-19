package com.urlshortener.controller;

import com.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        log.info("Redirect request for shortCode: {}", shortCode);

        String originalUrl = urlService.resolveShortCode(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)        // 302 redirect
                .location(URI.create(originalUrl))
                .build();
    }
}