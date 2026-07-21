package com.urlshortener.repository;

import com.urlshortener.domain.entity.ClickEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClickEventRepository extends MongoRepository<ClickEvent, String> {

    List<ClickEvent> findByShortCode(String shortCode);
    List<ClickEvent> findByUrlId(String urlId);
    long countByShortCode(String shortCode);
}