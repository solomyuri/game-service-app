package com.solomyuri.game_service.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.solomyuri.game_service.model.dto.UserDto;

public interface UsersService {

	UserDto getUserFromToken(JwtAuthenticationToken token);
}
