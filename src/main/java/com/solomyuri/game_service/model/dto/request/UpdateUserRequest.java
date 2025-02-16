package com.solomyuri.game_service.model.dto.request;

import java.util.Optional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private Optional<@Email @NotNull String> email;

}
