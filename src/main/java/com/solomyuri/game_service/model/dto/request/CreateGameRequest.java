package com.solomyuri.game_service.model.dto.request;

import com.solomyuri.game_service.model.dto.GameDto;

import lombok.Data;

@Data
public class CreateGameRequest {
	
	private GameDto game;

}
