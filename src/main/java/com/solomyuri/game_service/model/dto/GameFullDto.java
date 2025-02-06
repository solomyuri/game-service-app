package com.solomyuri.game_service.model.dto;

import lombok.Data;

import java.util.Set;

@Data
public class GameFullDto {

    private Set<ShipDto> userShips;
    private Set<ShipDto> enemyShipsOpened;
    private Set<CellFullDto> userCellsOpened;
    private Set<CellFullDto> enemyCellsOpened;
    private boolean isUserTurn;

}
