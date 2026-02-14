package com.panvel.mneubarth.albumratings.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.panvel.mneubarth.albumratings.exceptions.TokenException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final WebClient.Builder builder;

    @Value("${spotify.auth.client-id}")
    private String clientId;

    @Value("${spotify.auth.client-secret}")
    private String clientSecret;

    private Mono<String> cachedToken = Mono.empty();

    @PostConstruct
    void init() {
        cachedToken = requestNewToken()
                .cache(token -> Duration.ofMinutes(59),  // expira automaticamente
                        throwable -> Duration.ZERO,
                        () -> Duration.ZERO);
    }

    public Mono<String> getToken() {
        return cachedToken
                .switchIfEmpty(Mono.defer(() -> {
                    cachedToken = requestNewToken()
                            .cache(token -> Duration.ofMinutes(59),
                                    err -> Duration.ZERO,
                                    () -> Duration.ZERO);
                    return cachedToken;
                }));
    }

    private Mono<String> requestNewToken() {
        return builder
                .baseUrl("https://accounts.spotify.com/api")
                .build()
                .post()
                .uri("/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters
                        .fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                )
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(json -> log.info("Resposta token: {}", json))
                .map(json -> json.get("access_token").asText())
                .switchIfEmpty(Mono.error(new TokenException("Failed to obtain Spotify token")))
                .doOnError(throwable -> log.info("Error obtaining Spotify token"))
                .doOnNext(token -> log.info("Novo token Spotify obtido!"));
    }
}
