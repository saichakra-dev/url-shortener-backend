package com.urlshortener.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "click_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent {

    @Id
    private String id;

    @Indexed
    private String shortCode;       // which URL was clicked

    @Indexed
    private String urlId;           // MongoDB id of the Url document

    private String ipAddress;       // where the click came from
    private String userAgent;       // browser/device info
    private String referer;         // where they came from

    @Builder.Default
    private Instant clickedAt = Instant.now();
}