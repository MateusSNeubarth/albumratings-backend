package com.panvel.mneubarth.albumratings.contract;

import com.panvel.mneubarth.albumratings.contract.mapper.AuthenticationMapperContract;
import com.panvel.mneubarth.albumratings.contract.request.RefreshTokenRequest;
import com.panvel.mneubarth.albumratings.contract.request.UserAuthenticationRequest;
import com.panvel.mneubarth.albumratings.contract.request.UserRegistrationRequest;
import com.panvel.mneubarth.albumratings.contract.response.UserAuthenticationResponse;
import com.panvel.mneubarth.albumratings.domain.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    //private final AuthService authService;

    private final AuthService authService;

/*    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponseDTO> register(@RequestBody UserRegisterRequestDTO request) {
        return authService.register(request);
    }
 */

    @PostMapping("/login")
    public Mono<UserAuthenticationResponse> authenticate(@RequestBody UserAuthenticationRequest request) {
        return authService.authenticate(request.toInput())
                .map(AuthenticationMapperContract::toUserAuthenticationResponse);
    }

    @PostMapping("/refresh")
    public Mono<UserAuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken())
                .map(AuthenticationMapperContract::toUserAuthenticationResponse);
    }

    @PostMapping("/register")
    public Mono<String> register(@RequestBody UserRegistrationRequest request) {
        return authService.createUser(request.toInput());
    }
}
