package com.solomyuri.game_service.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HomeResponse(UserDto user, PageResponseDto<UserDto> users) {
}
