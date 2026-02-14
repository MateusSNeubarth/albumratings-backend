package com.panvel.mneubarth.albumratings.controllers;

import com.panvel.mneubarth.albumratings.dto.request.LoginRequestDTO;
import com.panvel.mneubarth.albumratings.dto.request.UserRegisterRequestDTO;
import com.panvel.mneubarth.albumratings.dto.response.TokenResponseDTO;
import com.panvel.mneubarth.albumratings.dto.response.UserResponseDTO;
import com.panvel.mneubarth.albumratings.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponseDTO> register(@RequestBody UserRegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
