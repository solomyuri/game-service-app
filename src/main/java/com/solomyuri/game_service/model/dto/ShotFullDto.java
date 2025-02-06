package com.solomyuri.game_service.model.dto;

import com.solomyuri.game_service.enums.ShotResult;
import lombok.Data;

@Data
public class ShotFullDto {

    ShotResult result;
    CellDto cell;

}
