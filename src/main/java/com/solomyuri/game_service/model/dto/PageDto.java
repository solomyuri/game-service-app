package com.solomyuri.game_service.model.dto;

import java.util.List;

public record PageDto<T>(long totalElements, int pageNumber, int pageSize, List<T> content) {
}
