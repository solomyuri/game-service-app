package com.solomyuri.game_service.model.dto.request;

import com.solomyuri.game_service.model.dto.GameDto;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CreateGameRequest {
	@Valid
	private GameDto game;

}
