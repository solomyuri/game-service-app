package com.solomyuri.game_service.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solomyuri.game_service.model.dto.PageDto;
import com.solomyuri.game_service.model.dto.UserDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HomeResponse(UserDto user, PageDto<UserDto> users) {
}
