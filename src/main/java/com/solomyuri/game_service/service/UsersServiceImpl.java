package com.solomyuri.game_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomyuri.game_service.enums.Role;
import com.solomyuri.game_service.mapper.PageMapper;
import com.solomyuri.game_service.mapper.TokenMapper;
import com.solomyuri.game_service.mapper.UserMapper;
import com.solomyuri.game_service.model.TokenModel;
import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.dto.response.HomeResponse;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UserMapper userMapper;
    private final TokenMapper tokenMapper;
    private final PageMapper pageMapper;
    private final UsersRepository usersRepository;

    @Override
    @Transactional()
    public HomeResponse getUserByToken(JwtAuthenticationToken token, Pageable pageable) {

        TokenModel tokenModel = tokenMapper.jwtToModel(token);

        if (tokenModel.roles().contains(Role.ADMIN.getDescription()))
            return getUserForAdmin(tokenModel.username(), pageable);
        else
            return getUser(tokenModel.username());
    }

    private HomeResponse getUser(String username) {

        User userEntity = usersRepository.findByUsername(username).orElseGet(() ->
                usersRepository.save(User.builder().username(username).build()));

        return new HomeResponse(userMapper.userToDto(userEntity), null);
    }

    private HomeResponse getUserForAdmin(String username, Pageable pageable) {

        User userEntity = usersRepository.findByUsername(username).orElseGet(() ->
                usersRepository.save(User.builder().username(username).build()));

        Page<UserDto> pageUsers = usersRepository.findAll(pageable).map(userMapper::userToDto);
        return new HomeResponse(userMapper.userToDto(userEntity), pageMapper.pageToPageResponse(pageUsers));
    }

}
