package com.solomyuri.game_service.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solomyuri.game_service.model.dto.CellFullDto;
import com.solomyuri.game_service.model.dto.ShotFullDto;
import lombok.Data;

import java.util.LinkedList;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShotWsResponse {

    ShotFullDto userShot;
    LinkedList<ShotFullDto> enemyShots;
    Set<CellFullDto> enemyCellsOpen;
    Set<CellFullDto> userCellsOpen;

}
