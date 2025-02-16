package com.solomyuri.game_service.util;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class Constants {

    public final String SHIP_CELLS = "SHIP CELLS";
    public final String REMOVED_CELLS = "REMOVED CELLS";
    public final String BLOCKED = "BLOCKED";
    public final String UNBLOCKED = "UNBLOCKED";
    public final String USER_NOT_FOUND = "User not found";
    public final String GAME_NOT_FOUND = "Game not found";
    public final int AREA_SIZE = 10;
    
    public final Map<String, Integer> X_TO_NUMBER = Map.of(
            "A", 1, "B", 2, "C", 3, "D", 4, "E", 5,
            "F", 6, "G", 7, "H", 8, "I", 9, "J", 10
    );
    public final Map<Integer, String> NUMBER_TO_X = Map.of(
            1, "A", 2, "B", 3, "C", 4, "D", 5, "E",
            6, "F", 7, "G", 8, "H", 9, "I", 10, "J"
    );
}
