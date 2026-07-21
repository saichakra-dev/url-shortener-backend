package com.urlshortener.kafka.producer;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventMessage {
    private String urlId;
    private String shortCode;
    private String ipAddress;
    private String userAgent;
    private String referer;
    private Instant clickedAt;
}