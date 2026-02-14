package com.panvel.mneubarth.albumratings.services;

import com.panvel.mneubarth.albumratings.dto.request.KeycloakRegisterDTO;
import com.panvel.mneubarth.albumratings.dto.request.LoginRequestDTO;
import com.panvel.mneubarth.albumratings.dto.request.UserRegisterRequestDTO;
import com.panvel.mneubarth.albumratings.dto.response.TokenResponseDTO;
import com.panvel.mneubarth.albumratings.dto.response.UserResponseDTO;
import com.panvel.mneubarth.albumratings.exceptions.AuthException;
import com.panvel.mneubarth.albumratings.models.User;
import com.panvel.mneubarth.albumratings.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakAdminService keycloakAdminService;
    private final UserRepository userRepository;

    public Mono<UserResponseDTO> register(UserRegisterRequestDTO request) {
        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .build();

        return userRepository.save(user)
                .switchIfEmpty(Mono.error(new AuthException("Something went wrong during user registration")))
                .flatMap(savedUser -> {
                    KeycloakRegisterDTO keycloakRequest = KeycloakRegisterDTO.builder()
                            .userId(savedUser.getId())
                            .email(request.email())
                            .password(request.password())
                            .build();

                    return keycloakAdminService.createUser(keycloakRequest)
                            .thenReturn(UserResponseDTO.builder()
                                    .id(savedUser.getId())
                                    .name(savedUser.getName())
                                    .email(savedUser.getEmail())
                                    .albumRatings(savedUser.getAlbumRatings())
                                    .build()
                            );
                });
    }

    public Mono<TokenResponseDTO> login(LoginRequestDTO request) {
        return keycloakAdminService.getUserToken(request);
    }
}
