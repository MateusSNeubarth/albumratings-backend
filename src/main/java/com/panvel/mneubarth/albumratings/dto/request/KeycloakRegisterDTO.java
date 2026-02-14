package com.panvel.mneubarth.albumratings.dto.request;

import lombok.Builder;

@Builder
public record KeycloakRegisterDTO(
        String userId,
        String email,
        String password
) {
}
