package com.urlshortener.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.urlshortener.BaseIntegrationTest;
import com.urlshortener.dto.request.CreateUrlRequest;

class UrlControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should create URL when authenticated")
    void shouldCreateUrl_whenAuthenticated() throws Exception {

        String token = registerAndGetToken("test@example.com", "password123");

        CreateUrlRequest request = new CreateUrlRequest(
                "https://github.com",
                null,null
        );

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.shortUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.originalUrl")
                        .value("https://github.com"));
    }

    @Test
    @DisplayName("Should return 403 when no token provided")
    void shouldReturn403_whenNotAuthenticated() throws Exception {

        CreateUrlRequest request = new CreateUrlRequest(
                "https://github.com",
                null,null
        );

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should create URL with custom alias")
    void shouldCreateUrl_withCustomAlias() throws Exception {

        String token = registerAndGetToken("test@example.com", "password123");

        CreateUrlRequest request = new CreateUrlRequest(
                "https://github.com",
                "my-github",null
        );

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.shortCode")
                        .value("my-github"));
    }

    @Test
    @DisplayName("Should return user URLs")
    void shouldReturnUserUrls() throws Exception {

        String token = registerAndGetToken("test@example.com", "password123");

        CreateUrlRequest request1 = new CreateUrlRequest(
                "https://github.com",
                null,null
        );

        CreateUrlRequest request2 = new CreateUrlRequest(
                "https://spring.io",
                null,null
        );

        mockMvc.perform(post("/api/v1/urls")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/v1/urls")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/v1/urls")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("Should delete URL")
    void shouldDeleteUrl() throws Exception {

        String token = registerAndGetToken("test@example.com", "password123");

        CreateUrlRequest request = new CreateUrlRequest(
                "https://github.com",
                null,null
        );

        MvcResult result = mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();

        String id = JsonPath.read(response, "$.data.id");

        mockMvc.perform(delete("/api/v1/urls/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}