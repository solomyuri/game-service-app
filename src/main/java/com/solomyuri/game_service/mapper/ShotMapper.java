package com.solomyuri.game_service.mapper;

import com.solomyuri.game_service.model.dto.ShotFullDto;
import com.solomyuri.game_service.model.entity.Shot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CellMapper.class})
public interface ShotMapper {

    ShotFullDto entityToDto(Shot shot);

}
