package com.urlshortener.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!test")           // ← add this — skip entire config during tests
public class KafkaConfig {

    public static final String CLICK_EVENTS_TOPIC = "url.click.events";

    @Bean
    public NewTopic clickEventsTopic() {
        return TopicBuilder.name(CLICK_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}