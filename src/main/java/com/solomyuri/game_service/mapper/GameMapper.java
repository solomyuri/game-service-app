package com.solomyuri.game_service.mapper;

import org.mapstruct.Mapper;

import com.solomyuri.game_service.model.dto.GameDto;
import com.solomyuri.game_service.model.entity.Game;

@Mapper(componentModel = "spring", uses = {ShipMapper.class})
public interface GameMapper {
	
    Game dtoToEntity(GameDto dto);
}
