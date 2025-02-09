package com.solomyuri.game_service.config;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.solomyuri.game_service.exception.AuthErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthErrorHandler entryPoint) throws Exception {

	http.oauth2ResourceServer(
	        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

	http.authorizeHttpRequests(auth -> auth.anyRequest().hasAuthority("game-service-user"));

	http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .csrf(AbstractHttpConfigurer::disable);

	http.exceptionHandling(
	        exception -> exception.authenticationEntryPoint(entryPoint).accessDeniedHandler(entryPoint));

	return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

	var jwtAuthenticationConverter = new JwtAuthenticationConverter();
	jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);

	return jwtAuthenticationConverter;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

	return Optional.ofNullable(jwt.getClaimAsMap("resource_access"))
	        .map(Map::values)
	        .stream()
	        .flatMap(Collection::stream)
	        .map(clientAccess -> ((Map<String, Collection<String>>) clientAccess).get("roles"))
	        .filter(Objects::nonNull)
	        .flatMap(Collection::stream)
	        .map(SimpleGrantedAuthority::new)
	        .collect(Collectors.toList());
    }

}
