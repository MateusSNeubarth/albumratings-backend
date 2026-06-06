package com.panvel.mneubarth.albumratings.contract;

import com.panvel.mneubarth.albumratings.dto.request.RatingRequestDTO;
import com.panvel.mneubarth.albumratings.dto.response.AlbumResponse;
import com.panvel.mneubarth.albumratings.services.AlbumRatingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/album-rating")
@RequiredArgsConstructor
public class AlbumRatingController {

    private final AlbumRatingsService albumRatingsService;

    @PostMapping("/add-rating")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> addRating(@AuthenticationPrincipal Jwt jwt, @RequestBody RatingRequestDTO request) {
        return albumRatingsService.insertRating(jwt, request);
    }

    @GetMapping("/{albumId}")
    public Mono<AlbumResponse> findRatingByAlbumId(@PathVariable String albumId) {
        return albumRatingsService.findRatingsByAlbumId(albumId);
    }

    @Deprecated(forRemoval = true)
    @GetMapping
    public Flux<AlbumResponse> findAllAlbumsAverageRating() {
        return albumRatingsService.findAllAlbumsAverageRating();
    }
}
