package com.panvel.mneubarth.albumratings.infrastructure.database.repositories;

import com.panvel.mneubarth.albumratings.dto.response.UserAlbumResponse;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface UserRatingsCustomRepository {
    Mono<Page<UserAlbumResponse>> findAllUserRatings(int page, int limit);
}
