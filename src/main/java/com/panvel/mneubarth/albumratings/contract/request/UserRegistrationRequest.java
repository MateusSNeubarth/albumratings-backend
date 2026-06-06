package com.panvel.mneubarth.albumratings.contract.request;

import com.panvel.mneubarth.albumratings.domain.auth.dto.UserRegistrationInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequest {

    private String username;
    private String email;
    private String password;

    public UserRegistrationInput toInput() {
        return UserRegistrationInput.builder()
                .username(username)
                .password(password)
                .email(email)
                .build();
    }
}
