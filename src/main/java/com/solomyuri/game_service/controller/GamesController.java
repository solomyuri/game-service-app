package com.solomyuri.game_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solomyuri.game_service.model.dto.request.CreateGameRequest;
import com.solomyuri.game_service.model.dto.response.CreateGameResponse;
import com.solomyuri.game_service.service.GamesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("v1/battleship")
public class GamesController {

	private final GamesService gamesService;

	@PostMapping("")
	public ResponseEntity<CreateGameResponse> createGame(@Validated @RequestBody CreateGameRequest request,
	                                                     JwtAuthenticationToken authToken) {
		
		return new ResponseEntity<>(gamesService.createGame(request, authToken), HttpStatus.CREATED);
	}

}
