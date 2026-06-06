package com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak;

import com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.authenticate.response.KeycloakAuthenticationResponse;
import com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.exception.KeycloakErrorParser;
import com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.user.request.KeycloakUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakIntegration {

    private final WebClient keycloakWebClient;
    private final Environment environment;
    private final KeycloakErrorParser errorParser;

    public Mono<KeycloakAuthenticationResponse> authenticate(String username, String password) {
        return keycloakWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(environment.getRequiredProperty("keycloak.token-url"))
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", environment.getRequiredProperty("keycloak.client-id"))
                        .with("client_secret", environment.getRequiredProperty("keycloak.client-secret"))
                        .with("username", username)
                        .with("password", password)
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errorBody -> Mono.error(
                                        errorParser.parse(errorBody, response.statusCode())
                                ))
                )
                .bodyToMono(KeycloakAuthenticationResponse.class);
    }

    public Mono<KeycloakAuthenticationResponse> refreshToken(String refreshToken) {
        return keycloakWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(environment.getRequiredProperty("keycloak.token-url"))
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", environment.getRequiredProperty("keycloak.client-id"))
                        .with("client_secret", environment.getRequiredProperty("keycloak.client-secret"))
                        .with("refresh_token", refreshToken)
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errorBody -> Mono.error(
                                        errorParser.parse(errorBody, response.statusCode())
                                ))
                )
                .bodyToMono(KeycloakAuthenticationResponse.class);
    }

    public Mono<KeycloakAuthenticationResponse> authenticateClientCredentials() {
        return keycloakWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(environment.getRequiredProperty("keycloak.token-url"))
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", environment.getRequiredProperty("keycloak.client-id"))
                        .with("client_secret", environment.getRequiredProperty("keycloak.client-secret")))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errorBody -> Mono.error(
                                        errorParser.parse(errorBody, response.statusCode())
                                ))
                )
                .bodyToMono(KeycloakAuthenticationResponse.class);
    }

    public Mono<String> createUser(KeycloakUserRequest keycloakUserRequest) {
        return authenticateClientCredentials()
                .flatMap(authentication -> keycloakWebClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path(environment.getRequiredProperty("keycloak.admin-url"))
                                .build())
                        .header("Authorization", "Bearer " + authentication.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(keycloakUserRequest)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, response ->
                                response.bodyToMono(String.class)
                                        .defaultIfEmpty("")
                                        .flatMap(errorBody -> Mono.error(
                                                errorParser.parse(errorBody, response.statusCode())
                                        ))
                        )
                        .toBodilessEntity()
                )
                .map(this::extractUserIdFromLocationHeader);
    }

    private String extractUserIdFromLocationHeader(ResponseEntity<Void> responseEntity) {
        return Optional.ofNullable(responseEntity.getHeaders().getLocation())
                .map(URI::getPath)
                .map(location -> location.split("/users/"))
                .filter(locationSplitted -> locationSplitted.length == 2)
                .map(locationSplitted -> locationSplitted[1])
                .orElse(null);
    }
}
