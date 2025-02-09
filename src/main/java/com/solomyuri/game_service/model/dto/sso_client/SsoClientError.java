package com.solomyuri.game_service.model.dto.sso_client;

import lombok.Data;

@Data
public class SsoClientError {

    private Integer errorCode;
    private String errorDescription;
}
