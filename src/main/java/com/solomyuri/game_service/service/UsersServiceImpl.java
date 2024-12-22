package com.solomyuri.game_service.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.solomyuri.game_service.mapper.TokenMapper;
import com.solomyuri.game_service.mapper.UserMapper;
import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UserMapper userMapper;
    private final TokenMapper tokenMapper;
    private final UsersRepository usersRepository;

    @Override
    @Transactional()
    public UserDto getUserFromToken(JwtAuthenticationToken token) {

        String username = tokenMapper.jwtToModel(token).username();
        User userEntity = usersRepository.findByUsername(username).orElseGet(() ->
                usersRepository.save(new User(username, 0, 0, 0, null)));

        return userMapper.userToDto(userEntity);
    }

}
