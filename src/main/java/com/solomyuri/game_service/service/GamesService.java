package com.solomyuri.game_service.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.solomyuri.game_service.model.dto.request.CreateGameRequest;
import com.solomyuri.game_service.model.dto.response.CreateGameResponse;

public interface GamesService {

	CreateGameResponse createGame(CreateGameRequest request, JwtAuthenticationToken token);
}
