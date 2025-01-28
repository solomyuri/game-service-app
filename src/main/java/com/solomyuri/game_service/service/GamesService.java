package com.solomyuri.game_service.service;

import com.solomyuri.game_service.model.dto.CellDto;
import com.solomyuri.game_service.model.dto.request.CreateGameRequest;
import com.solomyuri.game_service.model.dto.response.CreateGameResponse;
import com.solomyuri.game_service.model.dto.response.ShotWsResponse;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

public interface GamesService {

	CreateGameResponse createGame(CreateGameRequest request, JwtAuthenticationToken token);
	Game getFullGame(UUID gameId, JwtAuthenticationToken token);
	void invokeShotsByMachine(Game game, ShotWsResponse response);
	void invokeShotsByUser(Game game, CellDto targetCellDto, ShotWsResponse response);
	boolean isWinner(Game game, Optional<User> userOpt);
	void gameFinishing(Game game, Optional<User> winner);

}
