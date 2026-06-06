package com.panvel.mneubarth.albumratings.services;

import com.panvel.mneubarth.albumratings.dto.request.KeycloakRegisterDTO;
import com.panvel.mneubarth.albumratings.dto.request.LoginRequestDTO;
import com.panvel.mneubarth.albumratings.dto.response.TokenResponseDTO;
import com.panvel.mneubarth.albumratings.exceptions.EntityNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

//@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final WebClient keycloakWebClient;

    @Value("${keycloak.admin-url}")
    private String adminUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.auth-url}")
    private String tokenUrl;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    public Mono<String> createUser(KeycloakRegisterDTO request) {
        return getAdminToken()
                .flatMap(token ->
                        createUserInKeycloak(token, request)
                                .flatMap(userId ->
                                        getRealmRole(token)
                                                .flatMap(role ->
                                                        assignRoleToUser(token, userId, role)
                                                                .thenReturn("User created and role assigned")
                                                )
                                )
                );
    }

    public Mono<String> createUserInKeycloak(String token, KeycloakRegisterDTO request) {
        return keycloakWebClient.post()
                .uri(adminUrl + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(Map.of(
                        "username", request.userId(),
                        "email", request.email(),
                        "enabled", true,
                        "credentials", List.of(Map.of(
                                "type", "password",
                                "temporary", false,
                                "value", request.password()
                        ))
                ))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        // The user ID comes in the Location header
                        String location = response.headers().asHttpHeaders().getFirst("Location");
                        String userId = location != null ? location.substring(location.lastIndexOf('/') + 1) : null;
                        if (userId == null) {
                            log.info("User ID not found in Location header");
                            throw new EntityNotFound("User not found");
                        }
                        return Mono.just(userId);
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.info("Failed to create user: {}", body);
                                    return Mono.error(new RuntimeException("User creation failed: " + body));
                                });
                    }
                });
    }

    public Mono<TokenResponseDTO> getUserToken(LoginRequestDTO request) {
        return keycloakWebClient.post()
                .uri(tokenUrl)
                .body(
                        BodyInserters.fromFormData("grant_type", "password")
                                .with("client_id", clientId)
                                .with("client_secret", clientSecret)
                                .with("username", request.email())
                                .with("password", request.password())
                )
                .retrieve()
                .bodyToMono(TokenResponseDTO.class)
                .doOnError(throwable -> log.info("Error obtaining user token: {}", throwable.getMessage()));
    }

    private Mono<Void> assignRoleToUser(String token, String userId, Map<String, Object> roleRepresentation) {
        return keycloakWebClient.post()
                .uri(adminUrl + "/users/" + userId + "/role-mappings/realm")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(List.of(roleRepresentation))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> log.info("Error assigning role to user: {}", error.getMessage()));
    }

    private Mono<Map<String, Object>> getRealmRole(String token) {
        return keycloakWebClient.get()
                .uri(adminUrl + "/roles/" + "user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }


    private Mono<String> getAdminToken() {
        return keycloakWebClient.post()
                .uri(tokenUrl)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("username", adminUsername)
                        .with("password", adminPassword))
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> resp.get("access_token").toString());
    }
}
