package com.solomyuri.game_service.model;

import java.util.List;

import lombok.Builder;

@Builder
public record TokenModel(String username, List<String> roles, boolean emailVerified, String email) {

}
