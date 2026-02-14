package com.panvel.mneubarth.albumratings.dto.request;

import java.math.BigDecimal;

public record RatingRequestDTO(String albumId, BigDecimal rating, String comment) {
}
