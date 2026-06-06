package com.panvel.mneubarth.albumratings.services;

import com.panvel.mneubarth.albumratings.dto.request.RatingRequestDTO;
import com.panvel.mneubarth.albumratings.dto.response.UserAlbumResponse;
import com.panvel.mneubarth.albumratings.dto.response.UserResponseDTO;
import com.panvel.mneubarth.albumratings.infrastructure.database.models.AlbumRatings;
import com.panvel.mneubarth.albumratings.infrastructure.database.models.User;
import com.panvel.mneubarth.albumratings.infrastructure.database.models.UserAlbumRatings;
import com.panvel.mneubarth.albumratings.exceptions.EntityNotFound;
import com.panvel.mneubarth.albumratings.infrastructure.database.repositories.AlbumRatingsRepository;
import com.panvel.mneubarth.albumratings.infrastructure.database.repositories.UserRatingsCustomRepository;
import com.panvel.mneubarth.albumratings.infrastructure.database.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final AlbumRatingsRepository albumRatingsRepository;

    private final UserRatingsCustomRepository userRatingsCustomRepository;

    public Mono<UserResponseDTO> findById(Jwt jwt) {
        return userRepository.findById(jwt.getClaims().get("preferred_username").toString())
                .switchIfEmpty(Mono.error(new AccountNotFoundException("User not found")))
                .doOnError(throwable -> log.info("User not found with id {}", jwt.getClaims().get("preferred_username").toString()))
                .map(user -> UserResponseDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .albumRatings(user.getAlbumRatings())
                        .build());
    }

    public Flux<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .map(user -> UserResponseDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .albumRatings(user.getAlbumRatings())
                        .build());
    }

    public Mono<Page<UserAlbumResponse>> findAllUserRatings(int page, int limit) {
        return userRatingsCustomRepository.findAllUserRatings(page, limit);
    }

    public Flux<UserAlbumResponse> findRatingByAlbumId(String albumId) {
        return userRepository.findRatingByAlbumId(albumId);
    }

    public Mono<Void> updateRating(Jwt jwt, RatingRequestDTO request) {
        return userRepository.findById(jwt.getClaims().get("preferred_username").toString())
                .map(object -> object)
                .switchIfEmpty(Mono.error(new EntityNotFound("User Not Found")))
                .doOnError(throwable -> log.info("User not found with id {}", jwt.getClaims().get("preferred_username").toString()))
                .zipWith(albumRatingsRepository.findBySpotifyAlbumId(request.albumId()))
                .switchIfEmpty(Mono.error(new EntityNotFound("Album Not Found")))
                .flatMap(tuple -> {
                    AlbumRatings album = tuple.getT2();
                    User user = tuple.getT1();

                    UserAlbumRatings toUpdate = user.getAlbumRatings().stream().
                            filter(rating -> Objects.equals(rating.getSpotifyAlbumId(), album.getSpotifyAlbumId()))
                            .findFirst().orElse(null);
                    if (toUpdate == null) {
                        log.info("Album ID not found on user ratings list {}", request.albumId());
                        return Mono.error(new EntityNotFound("No such album on user ratings list"));
                    }
                    album.setRating(updateRating(album.getRating(), toUpdate.getRating(), request.rating(), album.getTotalRatings() - 1));
                    user.getAlbumRatings().remove(toUpdate);
                    toUpdate.setRating(request.rating());
                    toUpdate.setComment(request.comment());
                    user.getAlbumRatings().add(toUpdate);


                    return userRepository.save(user)
                            .then(albumRatingsRepository.save(album))
                            .then();
                });
    }

    private BigDecimal updateRating(BigDecimal currentAvgRating, BigDecimal oldRating, BigDecimal newRating, Long totalRatings) {
        if (totalRatings == 0) {
            return newRating.setScale(2, RoundingMode.HALF_UP);
        }
        return ((currentAvgRating.multiply(BigDecimal.valueOf(totalRatings)).subtract(oldRating)).add(newRating))
                .divide(
                        BigDecimal.valueOf(totalRatings),
                        2,
                        RoundingMode.HALF_UP
                );
    }
}
