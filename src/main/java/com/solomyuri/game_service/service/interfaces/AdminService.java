package com.solomyuri.game_service.service.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.solomyuri.game_service.model.dto.PageDto;
import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.dto.request.SearchUsersRequest;

public interface AdminService {

    PageDto<UserDto> getUsers(SearchUsersRequest request, Pageable pageable, JwtAuthenticationToken jwtToken);

    String blocking(String username, JwtAuthenticationToken jwtToken);

    String changeAdminRole(String username, JwtAuthenticationToken jwtToken);

}
