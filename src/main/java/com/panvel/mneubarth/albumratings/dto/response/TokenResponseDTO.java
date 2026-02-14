package com.panvel.mneubarth.albumratings.dto.response;

import lombok.Builder;

@Builder
public record TokenResponseDTO(
        String access_token,
        String refresh_token,
        String token_type,
        long expires_in,
        long refresh_expires_in,
        String scope
) {
}
