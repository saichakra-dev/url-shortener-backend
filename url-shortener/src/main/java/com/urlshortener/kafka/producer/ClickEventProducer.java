package com.urlshortener.kafka.producer;

import com.urlshortener.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickEventProducer {

    private final KafkaTemplate<String, ClickEventMessage> kafkaTemplate;

    public void publishClickEvent(ClickEventMessage message) {
        // Use shortCode as partition key
        // Same shortCode always goes to same partition = ordered events per URL
        CompletableFuture<SendResult<String, ClickEventMessage>> future =
                kafkaTemplate.send(
                        KafkaConfig.CLICK_EVENTS_TOPIC,
                        message.getShortCode(),   // partition key
                        message
                );

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish click event for shortCode: {} error: {}",
                        message.getShortCode(), ex.getMessage());
            } else {
                log.debug("Published click event for shortCode: {} partition: {} offset: {}",
                        message.getShortCode(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}