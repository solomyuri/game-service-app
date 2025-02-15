package com.solomyuri.game_service.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomyuri.game_service.client.SsoClient;
import com.solomyuri.game_service.enums.Role;
import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.mapper.PageMapper;
import com.solomyuri.game_service.mapper.TokenMapper;
import com.solomyuri.game_service.mapper.UserMapper;
import com.solomyuri.game_service.model.dto.TokenModel;
import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.dto.request.UpdateUserRequest;
import com.solomyuri.game_service.model.dto.response.HomeResponse;
import com.solomyuri.game_service.model.dto.sso_client.UserInfoResponse;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.UsersRepository;
import com.solomyuri.game_service.service.interfaces.UsersService;

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
    private final SsoClient ssoClient;

    @Override
    @Transactional
    public HomeResponse getUserByToken(JwtAuthenticationToken token, Pageable pageable) {

        TokenModel tokenModel = tokenMapper.jwtToModel(token);

        if (tokenModel.roles().contains(Role.ADMIN.getDescription()))
            return getUserForAdmin(tokenModel.username(), pageable);
        else
            return getUser(tokenModel.username());
    }
    
    @Override
    @Transactional
    public UserDto updateUser(JwtAuthenticationToken token, UpdateUserRequest request) {
	User userForUpdate = findForUpdateOrDelete(tokenMapper.jwtToModel(token).username());
	
	return userMapper.userToDto(userForUpdate);
    }
    
    @Override
    @Transactional
    public void deleteUser(JwtAuthenticationToken token) {
	User userForDelete = findForUpdateOrDelete(tokenMapper.jwtToModel(token).username());
	ssoClient.deleteUser(userForDelete.getUsername());
	usersRepository.deleteUser(userForDelete.getUsername());
    }
    
    private User findForUpdateOrDelete(String username) {

	return usersRepository.findByUsername(username).orElseThrow(() -> {
	    log.warn("User with username {} not found", username);
	    return new ApplicationException("User not found", HttpStatus.NOT_FOUND);
	});
    }

    private HomeResponse getUser(String username) {

	User userEntity = usersRepository.findByUsername(username).orElseGet(() -> {
	    checkBeforeCreate(username);
	    return usersRepository.save(User.builder().username(username).build());
	});

	return new HomeResponse(userMapper.userToDto(userEntity), null);
    }

    private HomeResponse getUserForAdmin(String username, Pageable pageable) {

	User userEntity = usersRepository.findByUsername(username).orElseGet(() -> {
	    checkBeforeCreate(username);
	    return usersRepository.save(User.builder().username(username).build());
	});

	Page<UserDto> pageUsers = usersRepository.findAll(pageable).map(userMapper::userToDto);
	return new HomeResponse(userMapper.userToDto(userEntity), pageMapper.pageToPageResponse(pageUsers));
    }
    
    private void checkBeforeCreate(String username) {
	List<UserInfoResponse> userInfo = ssoClient.getUser(username);
	if (Objects.isNull(userInfo) || userInfo.isEmpty() ||
	        !userInfo.get(0).getUsername().equals(username) || !userInfo.get(0).isEnabled()) {
	    throw new ApplicationException("User not found", HttpStatus.NOT_FOUND);
	}
    }

}
