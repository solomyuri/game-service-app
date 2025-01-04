package com.solomyuri.game_service.validation;

import com.solomyuri.game_service.model.dto.GameDto;

public interface GameValidator {

    void validateGameRequest(GameDto game);
}
