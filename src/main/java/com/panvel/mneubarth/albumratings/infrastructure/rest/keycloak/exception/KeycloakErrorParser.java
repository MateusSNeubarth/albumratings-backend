package com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panvel.mneubarth.albumratings.common.exception.AlbumRatingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@Slf4j
public class KeycloakErrorParser {

    private final ObjectMapper objectMapper;

    public KeycloakErrorParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AlbumRatingException parse(String errorBody, HttpStatusCode statusCode) {
        log.error("Error body from Keycloak: {}", errorBody);
        try {
            JsonNode root = objectMapper.readTree(errorBody);
            JsonNode data = root.path("data");

            String message = root.path("errorMessage").asText();
            String description = root.path("error_description").asText();

            if (data.has("errors")) {
                Set<String> details = new LinkedHashSet<>();
                extractAllErrorMessages(data.path("errors"), details);
                if (!details.isEmpty()) {
                    message = "Error with Keycloak integration: " + String.join("; ", details);
                }
            }

            if (description != null) {
                message = description;
            }

            return AlbumRatingException.builder()
                    .statusCode(statusCode)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.warn("Error parsing Keycloak response", e);
            return AlbumRatingException.builder()
                    .statusCode(statusCode)
                    .message("Error with Keycloak integration")
                    .build();
        }
    }

    private void extractAllErrorMessages(JsonNode node, Set<String> details) {
        if (node.isTextual()) {
            String text = node.asText();
            if (text != null && !text.isBlank()) {
                details.add(text);
            }
        } else if (node.isObject()) {
            for (JsonNode field : node) {
                extractAllErrorMessages(field, details);
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                extractAllErrorMessages(item, details);
            }
        }
    }
}
