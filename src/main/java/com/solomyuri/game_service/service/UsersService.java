package com.solomyuri.game_service.service;

import com.solomyuri.game_service.model.dto.HomeResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface UsersService {

	HomeResponse getUserByToken(JwtAuthenticationToken token, Pageable pageable);
}
