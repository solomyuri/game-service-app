package com.solomyuri.game_service.model;

import lombok.Builder;

@Builder
public record ErrorResponse(int errorCode, String errorDescription) {

}
