package com.solomyuri.game_service.model.dto;

import java.util.Set;

import com.solomyuri.game_service.enums.ShipType;

import lombok.Data;

@Data
public class ShipDto {

	private ShipType type;
	private Integer number;
	private Set<CellDto> cells;
}
