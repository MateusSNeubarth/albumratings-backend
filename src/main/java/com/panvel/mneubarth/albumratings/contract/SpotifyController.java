package com.panvel.mneubarth.albumratings.contract;

import com.panvel.mneubarth.albumratings.dto.response.AlbumResponse;
import com.panvel.mneubarth.albumratings.services.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/search")
    public Flux<AlbumResponse> searchAlbums(@RequestParam String query) {
        return spotifyService.searchAlbums(query);
    }

    @GetMapping("/find/{id}")
    public Mono<AlbumResponse> findAlbumById(@PathVariable String id) {
        return spotifyService.findAlbumById(id);
    }
}
