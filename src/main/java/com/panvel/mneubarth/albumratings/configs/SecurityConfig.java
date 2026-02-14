package com.panvel.mneubarth.albumratings.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/users/**").hasAnyRole("user", "admin")
                        .pathMatchers("/spotify/**").hasAnyRole("user", "admin")
                        .pathMatchers("/album-rating/**").hasAnyRole("user", "admin")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }


    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthConverter() {

        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        defaultConverter.setAuthorityPrefix("SCOPE_");

        return jwt -> {

            // 1. Get default authorities (like scopes)
            Collection<GrantedAuthority> defaultAuth = defaultConverter.convert(jwt);
            Collection<GrantedAuthority> authorities = new HashSet<>(defaultAuth);

            // 2. Extract Keycloak realm roles
            Object realmAccess = jwt.getClaim("realm_access");

            if (realmAccess instanceof Map<?, ?> map) {
                Object roles = map.get("roles");
                if (roles instanceof Collection<?> collection) {
                    collection.forEach(role ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString()))
                    );
                }
            }

            // 3. Return the reactive authentication token
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }

}
