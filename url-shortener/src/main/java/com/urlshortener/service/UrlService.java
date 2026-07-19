package com.urlshortener.service;

import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;

import java.util.List;

public interface UrlService {
    UrlResponse createUrl(CreateUrlRequest request, String userId);
    UrlResponse getUrlById(String id, String userId);
    List<UrlResponse> getUserUrls(String userId);
    void deleteUrl(String id, String userId);
    String resolveShortCode(String shortCode);  // returns original URL for redirect
}