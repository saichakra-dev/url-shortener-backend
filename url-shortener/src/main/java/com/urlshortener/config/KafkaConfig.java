package com.urlshortener.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String CLICK_EVENTS_TOPIC = "url.click.events";

    @Bean
    public NewTopic clickEventsTopic() {
        return TopicBuilder.name(CLICK_EVENTS_TOPIC)
                .partitions(3)      // 3 partitions = 3 consumers can read in parallel
                .replicas(1)        // 1 replica (we only have 1 Kafka broker in dev)
                .build();
    }
}