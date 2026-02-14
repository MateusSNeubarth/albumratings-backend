package com.panvel.mneubarth.albumratings.dto.response;

import com.panvel.mneubarth.albumratings.models.UserAlbumRatings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    private String id;
    private String email;
    private String name;
    private Set<UserAlbumRatings> albumRatings;
}
