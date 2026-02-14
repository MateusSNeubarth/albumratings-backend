package com.panvel.mneubarth.albumratings.configs;

import com.panvel.mneubarth.albumratings.services.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${spotify.api.base-url}")
    private String baseUrl;

    @Value("${keycloak.url")
    private String keycloakUrl;

    @Bean
    public WebClient keycloakWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(keycloakUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    @Bean
    public WebClient spotifyWebClient(WebClient.Builder builder, TokenService tokenService) {
        return builder
                .baseUrl(baseUrl)
                .filter(addAuthorizationHeader(tokenService))
                .build();
    }

    private ExchangeFilterFunction addAuthorizationHeader(TokenService tokenService) {
        return (request, next) ->
                tokenService.getToken()
                        .map(token -> ClientRequest.from(request)
                                .header("Authorization", "Bearer " + token)
                                .build())
                        .flatMap(next::exchange);
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response: {}", clientResponse.headers().asHttpHeaders().get("property-header"));
            log.info("Response: {}", clientResponse.headers());
            log.info("Response: rawStatusCode {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
