package com.solomyuri.game_service.model.dto;

import java.util.Set;

import lombok.Builder;

@Builder
public record TokenModel(String username, Set<String> roles) {

}
