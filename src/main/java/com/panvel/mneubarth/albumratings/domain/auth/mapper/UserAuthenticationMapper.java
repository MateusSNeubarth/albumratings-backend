package com.panvel.mneubarth.albumratings.domain.auth.mapper;

import com.panvel.mneubarth.albumratings.domain.auth.dto.UserAuthenticationInput;
import com.panvel.mneubarth.albumratings.domain.auth.dto.UserAuthenticationOutput;
import com.panvel.mneubarth.albumratings.domain.auth.dto.UserRegistrationInput;
import com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.authenticate.response.KeycloakAuthenticationResponse;
import com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.user.request.KeycloakUserRequest;

import java.util.List;

public class UserAuthenticationMapper {

    public static UserAuthenticationOutput toUserAuthenticationOutput(KeycloakAuthenticationResponse keycloakAuthenticationResponse) {
        return UserAuthenticationOutput.builder()
                .accessToken(keycloakAuthenticationResponse.getAccessToken())
                .refreshToken(keycloakAuthenticationResponse.getRefreshToken())
                .expiresIn(keycloakAuthenticationResponse.getExpiresIn())
                .refreshExpiresIn(keycloakAuthenticationResponse.getRefreshExpiresIn())
                .tokenType(keycloakAuthenticationResponse.getTokenType())
                .build();
    }

    public static KeycloakUserRequest toKeycloakUserRequest(UserRegistrationInput userRegistrationInput) {
        return KeycloakUserRequest.builder()
                .username(userRegistrationInput.getUsername())
                .email(userRegistrationInput.getEmail())
                .credentials(List.of(KeycloakUserRequest.CredentialRequest.builder()
                        .value(userRegistrationInput.getPassword())
                        .build()))
                .build();
    }
}
