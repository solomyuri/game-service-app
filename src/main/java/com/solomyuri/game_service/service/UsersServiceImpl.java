package com.solomyuri.game_service.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomyuri.game_service.client.SsoClient;
import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.mapper.TokenMapper;
import com.solomyuri.game_service.mapper.UserMapper;
import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.dto.request.UpdateUserRequest;
import com.solomyuri.game_service.model.dto.response.HomeResponse;
import com.solomyuri.game_service.model.dto.sso_client.EditUserRequest;
import com.solomyuri.game_service.model.dto.sso_client.UserInfoResponse;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.UsersRepository;
import com.solomyuri.game_service.service.interfaces.UsersService;
import com.solomyuri.game_service.util.AppUtil;
import com.solomyuri.game_service.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UserMapper userMapper;
    private final TokenMapper tokenMapper;
    private final UsersRepository usersRepository;
    private final SsoClient ssoClient;

    @Override
    @Transactional
    public HomeResponse getUserByToken(JwtAuthenticationToken token) {
	String username = tokenMapper.jwtToModel(token).username();

	User user = usersRepository.findByUsernameWithGame(username).orElseGet(() -> {
	    checkBeforeCreate(username);
	    return usersRepository.save(User.builder().username(username).build());
	});

	AppUtil.checkUserBlocked(user);

	return new HomeResponse(userMapper.toDto(user));
    }

    @Override
    @Transactional
    public UserDto updateUser(JwtAuthenticationToken token, UpdateUserRequest request) {

	User userForUpdate = getUserForUpdate(tokenMapper.jwtToModel(token).username());
	AppUtil.checkUserBlocked(userForUpdate);

	if (updateField(userForUpdate::setEmail, userForUpdate.getEmail(), request.getEmail())) {
	    EditUserRequest editUserRequest = EditUserRequest.builder().email(request.getEmail().orElse(null)).build();
	    ssoClient.editUser(userForUpdate.getUsername(), editUserRequest);
	}

	return userMapper.toDto(userForUpdate);
    }

    @Override
    @Transactional
    public void deleteUser(JwtAuthenticationToken token) {
	User userForDelete = getUserForUpdate(tokenMapper.jwtToModel(token).username());
	AppUtil.checkUserBlocked(userForDelete);
	ssoClient.deleteUser(userForDelete.getUsername());
	usersRepository.deleteUser(userForDelete.getUsername());
    }

    @Override
    @Transactional
    public User getUserForUpdate(String username) {

	User user = usersRepository.findForUpdate(username).orElseThrow(() -> {
	    log.warn("User with username {} not found", username);
	    return new ApplicationException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
	});

	return user;
    }

    private void checkBeforeCreate(String username) {
	List<UserInfoResponse> userInfo = ssoClient.getUser(username);
	if (Objects.isNull(userInfo) || userInfo.isEmpty() ||
	        !userInfo.get(0).getUsername().equals(username) || !userInfo.get(0).isEnabled()) {
	    throw new ApplicationException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
	}
    }

    private <T, V> boolean updateField(Consumer<V> consumer, T field, Optional<V> valueOpt) {
	if (Objects.isNull(valueOpt) ||
	        (Objects.isNull(field) && valueOpt.isEmpty()) ||
	        (Objects.nonNull(field) && valueOpt.isPresent() && Objects.equals(field, valueOpt.get()))) {
	    return false;
	} else {
	    consumer.accept(valueOpt.orElse(null));
	    return true;
	}
    }

}
