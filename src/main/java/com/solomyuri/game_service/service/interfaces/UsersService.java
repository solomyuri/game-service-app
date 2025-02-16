package com.solomyuri.game_service.service.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.dto.request.UpdateUserRequest;
import com.solomyuri.game_service.model.dto.response.HomeResponse;

public interface UsersService {

    HomeResponse getUserByToken(JwtAuthenticationToken token, Pageable pageable);

    UserDto updateUser(JwtAuthenticationToken token, UpdateUserRequest request);
    
    void deleteUser(JwtAuthenticationToken token);
}
