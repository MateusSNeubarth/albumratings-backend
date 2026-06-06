package com.panvel.mneubarth.albumratings.contract.request;

import com.panvel.mneubarth.albumratings.domain.auth.dto.UserAuthenticationInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthenticationRequest {

    private String username;
    private String password;

    public UserAuthenticationInput toInput() {
        return UserAuthenticationInput.builder()
                .username(username)
                .password(password)
                .build();
    }
}
