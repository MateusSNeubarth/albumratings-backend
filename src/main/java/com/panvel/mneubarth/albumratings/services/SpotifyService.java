package com.panvel.mneubarth.albumratings.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.panvel.mneubarth.albumratings.dto.response.AlbumResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final WebClient spotifyWebClient;

    public Flux<AlbumResponse> searchAlbums(String query) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("type", "album")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMapMany(json -> Flux.fromIterable(json.path("albums").path("items")))
                .map(album -> {
                    AlbumResponse dto = new AlbumResponse();
                    dto.setId(album.path("id").asText());
                    dto.setName(album.path("name").asText());
                    dto.setSpotifyUrl(album.path("external_urls").path("spotify").asText());

                    JsonNode artistsNode = album.path("artists");
                    if (artistsNode.isArray()) {
                        List<String> artists = dto.getArtists();
                        artistsNode.forEach(artist -> artist.path("name").asText());

                        artistsNode.forEach(artist -> artists.add(artist.path("name").asText()));
                    }

                    // tenta pegar a imagem média (índice 1), se não existir, pega a primeira
                    JsonNode images = album.path("images");
                    if (images.isArray() && images.size() > 1) {
                        dto.setImageUrl(images.get(1).path("url").asText());
                    } else if (images.isArray() && !images.isEmpty()) {
                        dto.setImageUrl(images.get(0).path("url").asText());
                    }

                    return dto;
                });
    }

    public Mono<AlbumResponse> findAlbumById(String id) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/albums/" + id)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(album -> {
                    AlbumResponse dto = new AlbumResponse();
                    dto.setId(album.path("id").asText());
                    dto.setName(album.path("name").asText());
                    dto.setSpotifyUrl(album.path("external_urls").path("spotify").asText());

                    // tenta pegar a imagem média (índice 1), se não existir, pega a primeira
                    JsonNode images = album.path("images");
                    if (images.isArray() && images.size() > 1) {
                        dto.setImageUrl(images.get(1).path("url").asText());
                    } else if (images.isArray() && !images.isEmpty()) {
                        dto.setImageUrl(images.get(0).path("url").asText());
                    }

                    return dto;
                });
    }
}