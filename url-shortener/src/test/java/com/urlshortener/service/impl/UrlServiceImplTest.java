package com.urlshortener.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.urlshortener.domain.entity.Url;
import com.urlshortener.domain.enums.UrlStatus;
import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.exception.DuplicateResourceException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.service.CacheService;
import com.urlshortener.service.RateLimitService;
import com.urlshortener.service.impl.UrlServiceImpl;
import com.urlshortener.util.Base62Encoder;

@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private Base62Encoder base62Encoder;

    @Mock
    private CacheService cacheService;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private UrlServiceImpl urlService;

    // runs before each test via ReflectionTestUtils
    void injectBaseUrl() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");
    }

    // ── createUrl tests ───────────────────────────────────────────

    @Nested
    @DisplayName("createUrl")
    class CreateUrlTests {

        @Test
        @DisplayName("Should create URL with generated short code")
        void shouldCreateUrl_withGeneratedShortCode() {
            // Arrange
            injectBaseUrl();
            CreateUrlRequest request = new CreateUrlRequest(
                    "https://github.com", null, null);
            String userId = "user123";

            when(base62Encoder.generate()).thenReturn("aB3xK9");
            when(urlRepository.existsByShortCode("aB3xK9")).thenReturn(false);
            when(urlRepository.save(any(Url.class))).thenAnswer(inv -> {
                Url url = inv.getArgument(0);
                url = Url.builder()
                        .id("64abc")
                        .shortCode(url.getShortCode())
                        .originalUrl(url.getOriginalUrl())
                        .createdBy(url.getCreatedBy())
                        .status(UrlStatus.ACTIVE)
                        .clickCount(0)
                        .build();
                return url;
            });

            // Act
            UrlResponse response = urlService.createUrl(request, userId);

            // Assert
            assertThat(response.shortCode()).isEqualTo("aB3xK9");
            assertThat(response.originalUrl()).isEqualTo("https://github.com");
            assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/aB3xK9");
            verify(urlRepository).save(any(Url.class));
            verify(rateLimitService).checkRateLimit(userId);
        }

        @Test
        @DisplayName("Should create URL with custom alias")
        void shouldCreateUrl_withCustomAlias() {
            injectBaseUrl();
            CreateUrlRequest request = new CreateUrlRequest(
                    "https://github.com", "github", null);

            when(urlRepository.existsByCustomAlias("github")).thenReturn(false);
            when(urlRepository.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

            UrlResponse response = urlService.createUrl(request, "user123");

            assertThat(response.shortCode()).isEqualTo("github");
            verify(base62Encoder, never()).generate(); // should NOT generate random code
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when alias is taken")
        void shouldThrow_whenAliasAlreadyExists() {
            CreateUrlRequest request = new CreateUrlRequest(
                    "https://github.com", "github", null);

            when(urlRepository.existsByCustomAlias("github")).thenReturn(true);

            assertThatThrownBy(() -> urlService.createUrl(request, "user123"))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("alias");
        }
    }

    // ── resolveShortCode tests ────────────────────────────────────

    @Nested
    @DisplayName("resolveShortCode")
    class ResolveShortCodeTests {

        @Test
        @DisplayName("Should return cached URL on cache hit")
        void shouldReturnCachedUrl_onCacheHit() {
            when(cacheService.getCachedUrl("aB3xK9"))
                    .thenReturn("https://github.com");

            String result = urlService.resolveShortCode("aB3xK9");

            assertThat(result).isEqualTo("https://github.com");
            verify(urlRepository, never()).findByShortCode(anyString()); // DB not hit
        }

        @Test
        @DisplayName("Should query DB and cache result on cache miss")
        void shouldQueryDb_onCacheMiss() {
            Url url = Url.builder()
                    .id("64abc")
                    .shortCode("aB3xK9")
                    .originalUrl("https://github.com")
                    .status(UrlStatus.ACTIVE)
                    .build();

            when(cacheService.getCachedUrl("aB3xK9")).thenReturn(null);
            when(urlRepository.findByShortCode("aB3xK9")).thenReturn(Optional.of(url));

            String result = urlService.resolveShortCode("aB3xK9");

            assertThat(result).isEqualTo("https://github.com");
            verify(cacheService).cacheUrl("aB3xK9", "https://github.com");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for expired URL")
        void shouldThrow_forExpiredUrl() {
            Url expiredUrl = Url.builder()
                    .shortCode("aB3xK9")
                    .originalUrl("https://github.com")
                    .status(UrlStatus.ACTIVE)
                    .expiresAt(Instant.now().minusSeconds(3600)) // expired 1 hour ago
                    .build();

            when(cacheService.getCachedUrl("aB3xK9")).thenReturn(null);
            when(urlRepository.findByShortCode("aB3xK9"))
                    .thenReturn(Optional.of(expiredUrl));

            assertThatThrownBy(() -> urlService.resolveShortCode("aB3xK9"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for inactive URL")
        void shouldThrow_forInactiveUrl() {
            Url inactiveUrl = Url.builder()
                    .shortCode("aB3xK9")
                    .originalUrl("https://github.com")
                    .status(UrlStatus.INACTIVE)
                    .build();

            when(cacheService.getCachedUrl("aB3xK9")).thenReturn(null);
            when(urlRepository.findByShortCode("aB3xK9"))
                    .thenReturn(Optional.of(inactiveUrl));

            assertThatThrownBy(() -> urlService.resolveShortCode("aB3xK9"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── deleteUrl tests ───────────────────────────────────────────

    @Nested
    @DisplayName("deleteUrl")
    class DeleteUrlTests {

        @Test
        @DisplayName("Should delete URL and evict cache")
        void shouldDeleteUrl_andEvictCache() {
            Url url = Url.builder()
                    .id("64abc")
                    .shortCode("aB3xK9")
                    .createdBy("user123")
                    .build();

            when(urlRepository.findById("64abc")).thenReturn(Optional.of(url));

            urlService.deleteUrl("64abc", "user123");

            verify(urlRepository).delete(url);
            verify(cacheService).evictUrl("aB3xK9");
        }

        @Test
        @DisplayName("Should throw when deleting another user's URL")
        void shouldThrow_whenDeletingAnotherUsersUrl() {
            Url url = Url.builder()
                    .id("64abc")
                    .shortCode("aB3xK9")
                    .createdBy("user123")
                    .build();

            when(urlRepository.findById("64abc")).thenReturn(Optional.of(url));

            assertThatThrownBy(() -> urlService.deleteUrl("64abc", "differentUser"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(urlRepository, never()).delete(any());
        }
    }

    // ── getUserUrls tests ─────────────────────────────────────────

    @Nested
    @DisplayName("getUserUrls")
    class GetUserUrlsTests {

        @Test
        @DisplayName("Should return all URLs for user")
        void shouldReturnAllUrls_forUser() {
            injectBaseUrl();
            List<Url> urls = List.of(
                    Url.builder().id("1").shortCode("abc").originalUrl("https://a.com")
                            .createdBy("user123").status(UrlStatus.ACTIVE).clickCount(0).build(),
                    Url.builder().id("2").shortCode("def").originalUrl("https://b.com")
                            .createdBy("user123").status(UrlStatus.ACTIVE).clickCount(0).build()
            );

            when(urlRepository.findByCreatedBy("user123")).thenReturn(urls);

            List<UrlResponse> result = urlService.getUserUrls("user123");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).shortCode()).isEqualTo("abc");
            assertThat(result.get(1).shortCode()).isEqualTo("def");
        }

        @Test
        @DisplayName("Should return empty list when user has no URLs")
        void shouldReturnEmptyList_whenNoUrls() {
            injectBaseUrl();
            when(urlRepository.findByCreatedBy("user123")).thenReturn(List.of());

            List<UrlResponse> result = urlService.getUserUrls("user123");

            assertThat(result).isEmpty();
        }
    }
}