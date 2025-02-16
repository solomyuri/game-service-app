package com.solomyuri.game_service.model.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record TokenModel(String username, List<String> roles) {

}
