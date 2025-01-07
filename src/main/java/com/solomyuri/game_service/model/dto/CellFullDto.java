package com.solomyuri.game_service.model.dto;

import lombok.Data;

@Data
public class CellFullDto {

    private String x;
    private String y;
    private boolean isOpen;
    private boolean hasShip;
}
