package com.solomyuri.game_service.model.dto.sso_client;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditUserRequest {

    private String email;
    private Boolean emailVerified;
    private Boolean enabled;

}
