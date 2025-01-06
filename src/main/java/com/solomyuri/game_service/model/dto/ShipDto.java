package com.solomyuri.game_service.model.dto;

import com.solomyuri.game_service.enums.ShipType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class ShipDto {

	@NotNull(message = "type must be not null")
	private ShipType type;
	@NotNull(message = "number must be not null")
	private Integer number;
	@Valid
	@NotEmpty(message = "ships must be not empty")
	private Set<CellDto> cells;
}
