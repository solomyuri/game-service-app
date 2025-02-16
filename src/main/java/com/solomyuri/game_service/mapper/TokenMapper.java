package com.solomyuri.game_service.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.solomyuri.game_service.model.dto.TokenModel;

@Mapper(componentModel = "spring")
public interface TokenMapper {

    default TokenModel jwtToModel(JwtAuthenticationToken token) {
	Map<String, Object> tokenAttributes = token.getToken().getClaims();
	return TokenModel.builder()
	        .username((String) tokenAttributes.get("preferred_username"))
	        .roles(getRoles(token.getToken()))
	        .build();
    }

    @SuppressWarnings("unchecked")
    private Set<String> getRoles(Jwt jwt) {
	Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
	if (resourceAccess != null && resourceAccess.containsKey("game-service-app")) {
	    return ((Map<String, List<String>>) resourceAccess.get("game-service-app"))
	            .get("roles")
	            .stream()
	            .collect(Collectors.toSet());
	} else
	    return Set.of();
    }

}
