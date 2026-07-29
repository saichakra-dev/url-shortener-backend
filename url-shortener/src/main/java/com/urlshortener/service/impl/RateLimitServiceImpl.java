package com.urlshortener.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.urlshortener.exception.RateLimitExceededException;
import com.urlshortener.service.RateLimitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS_PER_HOUR = 10;

    @Override
    public void checkRateLimit(String userId) {
        String key = "rate:" + userId + ":" + getCurrentHour();

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // First request this hour — set TTL so key auto-deletes
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }

        log.debug("Rate limit check — user: {} count: {}/{}", 
                userId, count, MAX_REQUESTS_PER_HOUR);

        if (count > MAX_REQUESTS_PER_HOUR) {
            log.warn("Rate limit exceeded for user: {}", userId);
            throw new RateLimitExceededException(
                    "Rate limit exceeded: max " + MAX_REQUESTS_PER_HOUR 
                    + " URLs per hour");
        }
    }

    private String getCurrentHour() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
    }
}