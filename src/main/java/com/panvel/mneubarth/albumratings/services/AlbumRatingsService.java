package com.panvel.mneubarth.albumratings.services;

import com.panvel.mneubarth.albumratings.dto.request.RatingRequestDTO;
import com.panvel.mneubarth.albumratings.dto.response.AlbumResponse;
import com.panvel.mneubarth.albumratings.exceptions.EntityNotFound;
import com.panvel.mneubarth.albumratings.infrastructure.database.models.AlbumRatings;
import com.panvel.mneubarth.albumratings.infrastructure.database.models.User;
import com.panvel.mneubarth.albumratings.infrastructure.database.models.UserAlbumRatings;
import com.panvel.mneubarth.albumratings.infrastructure.database.repositories.AlbumRatingsRepository;
import com.panvel.mneubarth.albumratings.infrastructure.database.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumRatingsService {

    private final AlbumRatingsRepository albumRatingsRepository;

    private final UserRepository userRepository;

    private final SpotifyService spotifyService;

    public Mono<Void> addRating(RatingRequestDTO request) {

        return spotifyService.findAlbumById(request.albumId())
                .switchIfEmpty(Mono.error(new EntityNotFound("Album Not Found")))
                .zipWith(userRepository.findById("request.userId()")
                        .switchIfEmpty(Mono.error(new EntityNotFound("User Not Found"))))
                .flatMap(tuple -> {
                    AlbumResponse album = tuple.getT1();
                    User user = tuple.getT2();

                    user.getAlbumRatings().add(new UserAlbumRatings(album.getId(), request.rating(), request.comment(), album.getName(), album.getImageUrl()));

                    AlbumRatings albumRating = AlbumRatings.builder()
                            .spotifyAlbumId(album.getId())
                            .rating(request.rating())
                            .name(album.getName())
                            .imgUrl(album.getImageUrl())
                            .externalSpotifyUrl(album.getSpotifyUrl())
                            .build();

                    return userRepository.save(user)
                            .then(albumRatingsRepository.save(albumRating))
                            .then();
                });
    }

    public Mono<Void> insertRating(Jwt jwt, RatingRequestDTO request) {

        return userRepository.findById(jwt.getClaims().get("preferred_username").toString())
                .doOnNext(user -> {
                    if (user.getId().isEmpty()) {
                        log.info("Error finding user with id {}", jwt.getClaims().get("preferred_username").toString());
                        throw new EntityNotFound("User Not Found");
                    }
                    for (UserAlbumRatings ratedAlbum : user.getAlbumRatings()) {
                        if (ratedAlbum.getSpotifyAlbumId().equals(request.albumId())) {
                            log.info("User {} has already rated album {}", user.getId(), request.albumId());
                            throw new IllegalArgumentException("User has already rated this album");
                        }
                    }
                })
                .zipWith(albumRatingsRepository.findBySpotifyAlbumId(request.albumId())
                        .switchIfEmpty(
                                spotifyService.findAlbumById(request.albumId())
                                        .doOnError(throwable -> log.info("Error finding album with id {}", request.albumId()))
                                        .map(foundAlbum -> AlbumRatings.builder()
                                                .spotifyAlbumId(foundAlbum.getId())
                                                .name(foundAlbum.getName())
                                                .imgUrl(foundAlbum.getImageUrl())
                                                .externalSpotifyUrl(foundAlbum.getSpotifyUrl())
                                                .rating(request.rating())
                                                .totalRatings(1L)
                                                .build())
                        )
                        .flatMap(album -> {
                            if (album.getId() != null) {
                                album.setRating(updateAvgRating(
                                        album.getRating(),
                                        request.rating(),
                                        album.getTotalRatings()
                                ));
                                album.setTotalRatings(album.getTotalRatings() + 1);
                            }

                            return albumRatingsRepository.save(album);
                        }))
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    AlbumRatings album = tuple.getT2();

                    user.getAlbumRatings().add(
                            new UserAlbumRatings(album.getSpotifyAlbumId(), request.rating(), request.comment(), album.getName(), album.getImgUrl())
                    );

                    return userRepository.save(user);
                })
                .then();
    }

    public Mono<AlbumResponse> findRatingsByAlbumId(String albumId) {
        return albumRatingsRepository.findBySpotifyAlbumId(albumId).map(album -> AlbumResponse.builder()
                .id(album.getSpotifyAlbumId())
                .name(album.getName())
                .rating(album.getRating())
                .imageUrl(album.getImgUrl())
                .spotifyUrl(album.getExternalSpotifyUrl())
                .build());
    }

    @Deprecated
    public Flux<AlbumResponse> findAllAlbumsAverageRating() {
        return albumRatingsRepository.findAllAlbumsWithAvgRating();
    }

    private BigDecimal updateAvgRating(BigDecimal currentRating, BigDecimal newRating, Long totalRatings) {
        return (currentRating.multiply(BigDecimal.valueOf(totalRatings)).add(newRating))
                .divide(
                        BigDecimal.valueOf(totalRatings).add(BigDecimal.valueOf(1)),
                        2,
                        RoundingMode.HALF_UP
                );
    }
}
