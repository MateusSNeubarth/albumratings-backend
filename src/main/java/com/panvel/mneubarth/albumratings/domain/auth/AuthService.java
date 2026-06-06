package com.panvel.mneubarth.albumratings.domain.auth;

import com.panvel.mneubarth.albumratings.domain.auth.dto.UserAuthenticationInput;
import com.panvel.mneubarth.albumratings.domain.auth.dto.UserAuthenticationOutput;
import com.panvel.mneubarth.albumratings.domain.auth.dto.UserRegistrationInput;
import com.panvel.mneubarth.albumratings.domain.auth.mapper.UserAuthenticationMapper;
import com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.KeycloakIntegration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakIntegration keycloakIntegration;

    public Mono<UserAuthenticationOutput> authenticate(UserAuthenticationInput userInput) {
        return keycloakIntegration.authenticate(userInput.getUsername(), userInput.getPassword())
                .map(UserAuthenticationMapper::toUserAuthenticationOutput);
    }

    public Mono<UserAuthenticationOutput> refreshToken(String refreshToken) {
        return keycloakIntegration.refreshToken(refreshToken)
                .map(UserAuthenticationMapper::toUserAuthenticationOutput);
    }

    public Mono<String> createUser(UserRegistrationInput userRegistrationInput) {
        return keycloakIntegration.createUser(UserAuthenticationMapper.toKeycloakUserRequest(userRegistrationInput));
    }
}
