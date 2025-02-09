package com.solomyuri.game_service.model.dto.sso_client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoResponse {

    private String username;
    private boolean emailVerified;
    private boolean enabled;

}
