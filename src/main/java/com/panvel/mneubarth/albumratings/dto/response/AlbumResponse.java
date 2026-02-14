package com.panvel.mneubarth.albumratings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {

    private String id;
    private String name;
    private List<String> artists = new ArrayList<>();
    private BigDecimal rating;
    private String imageUrl;
    private String spotifyUrl;
}
