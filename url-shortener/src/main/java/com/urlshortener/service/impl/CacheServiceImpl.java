package com.urlshortener.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.urlshortener.config.RedisConfig;
import com.urlshortener.service.CacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String URL_KEY_PREFIX = "url:";

    @Override
    public void cacheUrl(String shortCode, String originalUrl) {
        String key = URL_KEY_PREFIX + shortCode;
        redisTemplate.opsForValue().set(
                key,
                originalUrl,
                RedisConfig.URL_CACHE_TTL.toMinutes(),
                TimeUnit.MINUTES
        );
        log.debug("Cached URL for shortCode: {}", shortCode);
    }

    @Override
    public String getCachedUrl(String shortCode) {
        String key = URL_KEY_PREFIX + shortCode;
        String value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("Cache HIT for shortCode: {}", shortCode);
        } else {
            log.debug("Cache MISS for shortCode: {}", shortCode);
        }
        return value;
    }

    @Override
    public void evictUrl(String shortCode) {
        String key = URL_KEY_PREFIX + shortCode;
        redisTemplate.delete(key);
        log.debug("Evicted cache for shortCode: {}", shortCode);
    }
}