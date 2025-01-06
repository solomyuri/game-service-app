package com.solomyuri.game_service.mapper;

import org.mapstruct.Mapper;

import com.solomyuri.game_service.model.dto.ShipDto;
import com.solomyuri.game_service.model.entity.Ship;

@Mapper(componentModel = "spring", uses = {CellMapper.class})
public interface ShipMapper {
	
    Ship dtoToEntity(ShipDto dto);
}
