package com.solomyuri.game_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.solomyuri.game_service.model.dto.ShipDto;
import com.solomyuri.game_service.model.entity.Ship;

@Mapper(componentModel = "spring", uses = { CellMapper.class })
public interface ShipMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "user", ignore = true)
    Ship dtoToEntity(ShipDto dto);

    ShipDto entityToDto(Ship dto);
}
