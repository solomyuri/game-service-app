package com.solomyuri.game_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    USER("game-service-user"),
    ADMIN("game-service-admin");

    private final String description;
}
