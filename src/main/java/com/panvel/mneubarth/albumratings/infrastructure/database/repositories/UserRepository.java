package com.panvel.mneubarth.albumratings.infrastructure.database.repositories;

import com.panvel.mneubarth.albumratings.dto.response.UserAlbumResponse;
import com.panvel.mneubarth.albumratings.infrastructure.database.models.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByEmail(String email);

    @Aggregation(pipeline = {
            "{ $unwind: '$albumRatings' }",
            "{ $count: 'total' }"
    })
    Mono<Long> countUserRatings();

    @Aggregation(pipeline = {
            "{ $unwind: '$albumRatings' }",
            "{ $project: { " +
                    "userId: '$_id', " +
                    "id: '$albumRatings.spotifyAlbumId', " +
                    "rating: '$albumRatings.rating', " +
                    "comment: '$albumRatings.comment' " +
                    "} }",
            "{ $skip: ?0 }",
            "{ $limit: ?1 }"
    })
    Flux<UserAlbumResponse> findAllUserRatings(long skip, long limit);

    @Aggregation(pipeline = {
            "{ $match: { 'albumRatings.spotifyAlbumId': ?0 } }",
            "{ $unwind: '$albumRatings' }",
            "{ $match: { 'albumRatings.spotifyAlbumId': ?0 } }",
            "{ $project: { " +
                    "userId: '$_id', " +
                    "id: '$albumRatings.spotifyAlbumId', " +
                    "rating: '$albumRatings.rating', " +
                    "comment: '$albumRatings.comment' " +
                    "} }"
    })
    Flux<UserAlbumResponse> findRatingByAlbumId(String albumId);

}
