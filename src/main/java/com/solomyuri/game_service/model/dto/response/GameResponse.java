package com.solomyuri.game_service.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.solomyuri.game_service.model.dto.GameFullDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameResponse {

    GameFullDto game;

    @JsonCreator
    public static GameResponse createResponse(GameFullDto game) {
	return new GameResponse(game);
    }

}
