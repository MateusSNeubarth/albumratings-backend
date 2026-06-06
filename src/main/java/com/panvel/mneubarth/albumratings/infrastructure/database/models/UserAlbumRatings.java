package com.panvel.mneubarth.albumratings.infrastructure.database.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAlbumRatings {

    private String spotifyAlbumId;
    private BigDecimal rating;
    private String comment;
    private String name;
    private String imgUrl;
}
