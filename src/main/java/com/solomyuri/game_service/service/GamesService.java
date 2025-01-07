package com.solomyuri.game_service.service;

import com.solomyuri.game_service.model.entity.Game;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.solomyuri.game_service.model.dto.request.CreateGameRequest;
import com.solomyuri.game_service.model.dto.response.CreateGameResponse;

import java.util.UUID;

public interface GamesService {

	CreateGameResponse createGame(CreateGameRequest request, JwtAuthenticationToken token);
	Game getFullGame(UUID gameId, JwtAuthenticationToken token);
}
