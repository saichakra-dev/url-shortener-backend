package com.urlshortener.kafka.consumer;

import com.urlshortener.domain.entity.ClickEvent;
import com.urlshortener.domain.entity.Url;
import com.urlshortener.kafka.producer.ClickEventMessage;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickEventConsumer {

    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;

    @KafkaListener(
            topics = "url.click.events",
            groupId = "url-shortener-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeClickEvent(ClickEventMessage message) {
        log.debug("Consumed click event for shortCode: {}", message.getShortCode());

        try {
            // Save individual click event
            ClickEvent event = ClickEvent.builder()
                    .shortCode(message.getShortCode())
                    .urlId(message.getUrlId())
                    .ipAddress(message.getIpAddress())
                    .userAgent(message.getUserAgent())
                    .referer(message.getReferer())
                    .clickedAt(message.getClickedAt())
                    .build();

            clickEventRepository.save(event);

            // Update click count on Url document
            urlRepository.findById(message.getUrlId()).ifPresent(url -> {
                url.setClickCount(url.getClickCount() + 1);
                urlRepository.save(url);
                log.debug("Updated click count for URL: {}", message.getUrlId());
            });

        } catch (Exception e) {
            log.error("Failed to process click event for shortCode: {} error: {}",
                    message.getShortCode(), e.getMessage());
            // In production: send to dead letter topic for retry
        }
    }
}