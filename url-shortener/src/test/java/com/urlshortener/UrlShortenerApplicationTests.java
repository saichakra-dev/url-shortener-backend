package com.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.urlshortener.kafka.producer.ClickEventProducer;
import com.urlshortener.service.CacheService;
import com.urlshortener.service.RateLimitService;

@SpringBootTest(
    properties = {
        "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
    }
)
@ActiveProfiles("test")
class UrlShortenerApplicationTests {

    @MockitoBean
    CacheService cacheService;

    @MockitoBean
    RateLimitService rateLimitService;

    @MockitoBean
    ClickEventProducer clickEventProducer;

    @Test
    void contextLoads() {
    }
}