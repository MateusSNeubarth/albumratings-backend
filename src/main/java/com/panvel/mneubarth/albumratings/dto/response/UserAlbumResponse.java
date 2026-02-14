package com.panvel.mneubarth.albumratings.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAlbumResponse {

    private String userId;
    private String id;
    private BigDecimal rating;
    private String comment;
}
