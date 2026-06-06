package com.panvel.mneubarth.albumratings.infrastructure.database.repositories;

import com.panvel.mneubarth.albumratings.dto.response.AlbumResponse;
import com.panvel.mneubarth.albumratings.infrastructure.database.models.AlbumRatings;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AlbumRatingsRepository extends ReactiveMongoRepository<AlbumRatings, String> {

    @Deprecated
    @Aggregation(pipeline = {
            "{ $match: { spotifyAlbumId: ?0 } }",
            "{ $group: { " +
                    " _id: \"$spotifyAlbumId\", " +
                    " avgRating: { $avg: { $toDouble: \"$rating\" } }, " +
                    " name: { $first: \"$name\" }, " +
                    " imgUrl: { $first: \"$imgUrl\" }, " +
                    " externalSpotifyUrl: { $first: \"$externalSpotifyUrl\" } " +
                    "} }",
            "{ $project: { " +
                    " _id: 0, " +
                    " id: \"$_id\", " +
                    " name: 1, " +
                    " rating: \"$avgRating\", " +
                    " imageUrl: \"$imgUrl\", " +
                    " spotifyUrl: \"$externalSpotifyUrl\" " +
                    "} }"
    })
    Mono<AlbumResponse> findDistinctSpotifyAlbumId(final String albumId);

    @Deprecated
    @Aggregation(pipeline = {
            "{ $group: { " +
                    " _id: \"$spotifyAlbumId\", " +
                    " avgRating: { $avg: { $toDouble: \"$rating\" } }, " +
                    " name: { $first: \"$name\" }, " +
                    " imgUrl: { $first: \"$imgUrl\" }, " +
                    " externalSpotifyUrl: { $first: \"$externalSpotifyUrl\" } " +
                    "} }",
            "{ $project: { " +
                    " _id: 0, " +
                    " id: \"$_id\", " +
                    " name: 1, " +
                    " rating: \"$avgRating\", " +
                    " imageUrl: \"$imgUrl\", " +
                    " spotifyUrl: \"$externalSpotifyUrl\" " +
                    "} }"
    })
    Flux<AlbumResponse> findAllAlbumsWithAvgRating();

    @Query("{ 'spotifyAlbumId' : ?0 }")
    Mono<AlbumRatings> findBySpotifyAlbumId(String id);
}
