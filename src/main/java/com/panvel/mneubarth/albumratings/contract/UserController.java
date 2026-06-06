package com.panvel.mneubarth.albumratings.contract;

import com.panvel.mneubarth.albumratings.dto.request.RatingRequestDTO;
import com.panvel.mneubarth.albumratings.dto.response.UserAlbumResponse;
import com.panvel.mneubarth.albumratings.dto.response.UserResponseDTO;
import com.panvel.mneubarth.albumratings.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @GetMapping("/user")
    public Mono<ResponseEntity<UserResponseDTO>> findById(@AuthenticationPrincipal Jwt jwt) {
        return userService.findById(jwt)
                .map(body -> ResponseEntity.ok().body(body))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserResponseDTO> findAll() {
        return userService.findAll();
    }

    @GetMapping("/ratings")
    public Mono<Page<UserAlbumResponse>> findAllUserRatings(@RequestParam int page, @RequestParam int limit) {
        return userService.findAllUserRatings(page, limit);
    }

    @GetMapping("/ratings/{albumId}")
    public Flux<UserAlbumResponse> findRatingByAlbumId(@PathVariable String albumId) {
        return userService.findRatingByAlbumId(albumId);
    }

    @PutMapping("/ratings")
    public Mono<Void> updateRating(@AuthenticationPrincipal Jwt jwt, @RequestBody RatingRequestDTO request) {
        return userService.updateRating(jwt, request);
    }
}
