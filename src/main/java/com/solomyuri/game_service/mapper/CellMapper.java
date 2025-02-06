package com.solomyuri.game_service.mapper;

import com.solomyuri.game_service.model.dto.CellFullDto;
import org.mapstruct.Mapper;

import com.solomyuri.game_service.model.dto.CellDto;
import com.solomyuri.game_service.model.entity.Cell;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CellMapper {
	
    Cell dtoToEntity(CellDto dto);

    @Mapping(target = "hasShip", expression = "java(cell.getShip() != null)")
    @Mapping(target = "open", expression = "java(cell.getIsOpen())")
    CellFullDto entityToFullDto(Cell cell);
}

