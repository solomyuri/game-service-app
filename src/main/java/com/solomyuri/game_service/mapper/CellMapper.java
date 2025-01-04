package com.solomyuri.game_service.mapper;

import org.mapstruct.Mapper;

import com.solomyuri.game_service.model.dto.CellDto;
import com.solomyuri.game_service.model.entity.Cell;

@Mapper(componentModel = "spring")
public interface CellMapper {
	
    Cell dtoToEntity(CellDto dto);
}

