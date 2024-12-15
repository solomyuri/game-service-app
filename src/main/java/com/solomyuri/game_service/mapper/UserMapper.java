package com.solomyuri.game_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "currentGame", expression = "java(user.getCurrentGame() != null ? user.getCurrentGame().getId() : null)")
	UserDto userToDto(User user);
}
