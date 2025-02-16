package com.solomyuri.game_service.mapper;

import java.util.List;
import java.util.Map;

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
	private List<String> getRoles(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess != null && realmAccess.containsKey("roles")) {
			return ((List<String>) realmAccess.get("roles")).stream().toList();
		}
		return List.of();
	}

}
