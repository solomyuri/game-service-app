package com.solomyuri.game_service.mapper;

public interface EntityPagingMapper <E, D> {
    D toDto(E entity);
}
