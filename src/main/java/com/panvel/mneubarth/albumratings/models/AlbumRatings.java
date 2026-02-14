package com.panvel.mneubarth.albumratings.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumRatings {

    @Id
    private String id;
    private String spotifyAlbumId;
    private String name;
    private BigDecimal rating;
    private String imgUrl;
    private String externalSpotifyUrl;
    private Long totalRatings;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
