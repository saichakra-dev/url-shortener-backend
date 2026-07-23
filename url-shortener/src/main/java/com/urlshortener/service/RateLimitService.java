package com.urlshortener.service;

public interface RateLimitService {
    void checkRateLimit(String userId);  // throws RateLimitExceededException if exceeded
}