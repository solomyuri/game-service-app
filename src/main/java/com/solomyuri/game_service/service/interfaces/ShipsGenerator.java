package com.solomyuri.game_service.service.interfaces;

import com.solomyuri.game_service.model.entity.Cell;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.Ship;

import java.util.Map;
import java.util.Set;

public interface ShipsGenerator {

    Set<Ship> generateShipsSet(Map<String, Cell> cells, Game game);
}
