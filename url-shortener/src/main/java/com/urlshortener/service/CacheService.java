package com.urlshortener.service;

public interface CacheService {
    void cacheUrl(String shortCode, String originalUrl);
    String getCachedUrl(String shortCode);
    void evictUrl(String shortCode);
}