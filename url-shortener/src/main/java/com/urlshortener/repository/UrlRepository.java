package com.urlshortener.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.urlshortener.domain.entity.Url;
import com.urlshortener.domain.enums.UrlStatus;

public interface UrlRepository extends MongoRepository<Url, String> {

    Optional<Url> findByShortCode(String shortCode);

    List<Url> findByCreatedBy(String userId);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String alias);

    List<Url> findByCreatedByAndStatus(String createdBy, UrlStatus status);
}
