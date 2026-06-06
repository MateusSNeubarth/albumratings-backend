package com.panvel.mneubarth.albumratings.contract.mapper;

import com.panvel.mneubarth.albumratings.contract.response.UserAuthenticationResponse;
import com.panvel.mneubarth.albumratings.domain.auth.dto.UserAuthenticationOutput;

public class AuthenticationMapperContract {

    public static UserAuthenticationResponse toUserAuthenticationResponse(UserAuthenticationOutput authentication) {
        return UserAuthenticationResponse.builder()
                .accessToken(authentication.getAccessToken())
                .refreshToken(authentication.getRefreshToken())
                .expiresIn(authentication.getExpiresIn())
                .refreshExpiresIn(authentication.getRefreshExpiresIn())
                .tokenType(authentication.getTokenType())
                .build();
    }
}
