package com.solomyuri.game_service.model.dto;

import java.util.Set;

import lombok.Data;

@Data
public class GameDto {

	private Set<ShipDto> ships;
}
