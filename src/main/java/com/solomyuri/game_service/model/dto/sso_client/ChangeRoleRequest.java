package com.solomyuri.game_service.model.dto.sso_client;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRoleRequest {

    private String username;
    private String action;
    private List<String> roles;

}
