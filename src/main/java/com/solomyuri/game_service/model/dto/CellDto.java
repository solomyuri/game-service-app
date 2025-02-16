package com.solomyuri.game_service.model.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CellDto {

    @Pattern(regexp = "[A-J]", message = "x field must be included in [A-J]")
    private String x;
    @Pattern(regexp = "10|[1-9]", message = "y field must be included in [1-10]")
    private String y;

}
