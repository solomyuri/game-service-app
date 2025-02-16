package com.solomyuri.game_service.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solomyuri.game_service.model.dto.CellFullDto;
import com.solomyuri.game_service.model.dto.ShotFullDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShotWsResponse {

    private ShotFullDto userShot;
    private LinkedList<ShotFullDto> enemyShots = new LinkedList<>();
    private Set<CellFullDto> enemyCellsOpen = new HashSet<>();
    private Set<CellFullDto> userCellsOpen = new HashSet<>();
    private GameOver gameOver;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GameOver {
	private Boolean isVictory;
    }
}
