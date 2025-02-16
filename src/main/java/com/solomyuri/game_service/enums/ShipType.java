package com.solomyuri.game_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShipType {

    BATTLESHIP(4, 1),
    CRUISER(3, 2),
    SUBMARINE(2, 3),
    SLOOP(1, 4);

    private final int cellsCount;
    private final int shipsCount;
}
