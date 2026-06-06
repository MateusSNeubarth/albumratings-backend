package com.panvel.mneubarth.albumratings.infrastructure.rest.keycloak.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserRequest {

    private String username;
    private List<CredentialRequest> credentials;
    private String email;
    @Builder.Default
    private Boolean enabled = true;


    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CredentialRequest{

        @Builder.Default
        private Boolean temporary = false;
        @Builder.Default
        private String type = "password";
        private String value;
    }
}

