package com.solomyuri.game_service.model.dto;

import java.util.UUID;

public record UserDto (String username, int gameCount, int winCount, int loseCount, boolean isAdmin, UUID currentGame) {

}
