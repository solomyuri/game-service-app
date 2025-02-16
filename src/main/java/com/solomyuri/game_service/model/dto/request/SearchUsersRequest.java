package com.solomyuri.game_service.model.dto.request;

import lombok.Data;

@Data
public class SearchUsersRequest {

    private String username;
    private String email;

}
