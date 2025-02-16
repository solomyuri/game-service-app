package com.solomyuri.game_service.mapper;

import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper extends EntityPagingMapper<User, UserDto> {

    @Mapping(target = "currentGame", source = "currentGame.id")
    UserDto toDto(User user);

}
