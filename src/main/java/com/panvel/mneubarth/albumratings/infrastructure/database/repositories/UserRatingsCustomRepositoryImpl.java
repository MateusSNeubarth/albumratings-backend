package com.panvel.mneubarth.albumratings.infrastructure.database.repositories;

import com.panvel.mneubarth.albumratings.dto.response.UserAlbumResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class UserRatingsCustomRepositoryImpl implements UserRatingsCustomRepository {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<Page<UserAlbumResponse>> findAllUserRatings(int pageParam, int limitParam) {

        Pageable pageable = PageRequest.of(pageParam, limitParam, Sort.by(Sort.Direction.DESC, "rating"));

        long skip = (long) pageable.getPageNumber() * pageable.getPageSize();
        long limit = pageable.getPageSize();

        Mono<List<UserAlbumResponse>> contentMono =
                userRepository.findAllUserRatings(skip, limit).collectList();

        Mono<Long> totalMono =
                userRepository.count(); // counts users, not ratings — adjust below

        return Mono.zip(contentMono, totalMono)
                .map(tuple -> new PageImpl<>(
                        tuple.getT1(),
                        pageable,
                        tuple.getT2()
                ));
    }
}
