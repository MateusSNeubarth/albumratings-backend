package com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserResponse {

    private String id;
    private String username;
    private Long createdTimestamp;
    private String email;
    private String firstName;
    private String lastName;
}
