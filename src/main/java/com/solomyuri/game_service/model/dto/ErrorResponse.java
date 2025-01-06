package com.solomyuri.game_service.model.dto;

import lombok.Builder;

@Builder
public record ErrorResponse(int errorCode, String errorDescription) {

}
