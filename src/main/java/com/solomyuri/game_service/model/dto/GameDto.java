package com.solomyuri.game_service.model.dto;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class GameDto {

	@Valid
	@NotEmpty(message = "ships must be not empty")
	private Set<ShipDto> ships;
}
