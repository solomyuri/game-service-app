package com.solomyuri.game_service.service;

import static com.solomyuri.game_service.enums.Role.ADMIN;
import static com.solomyuri.game_service.util.Constants.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomyuri.game_service.client.SsoClient;
import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.mapper.PageMapper;
import com.solomyuri.game_service.mapper.TokenMapper;
import com.solomyuri.game_service.model.dto.PageDto;
import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.dto.request.SearchUsersRequest;
import com.solomyuri.game_service.model.dto.sso_client.ChangeRoleRequest;
import com.solomyuri.game_service.model.dto.sso_client.EditUserRequest;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.UsersRepository;
import com.solomyuri.game_service.repository.specification.UsersSpec;
import com.solomyuri.game_service.service.interfaces.AdminService;
import com.solomyuri.game_service.service.interfaces.UsersService;
import com.solomyuri.game_service.util.AppUtil;
import com.solomyuri.game_service.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UsersRepository usersRepository;
    private final UsersService usersService;
    private final SsoClient ssoClient;
    private final PageMapper pageMapper;
    private final TokenMapper tokenMapper;

    @Override
    @Transactional(readOnly = true)
    public PageDto<UserDto> getUsers(SearchUsersRequest request, Pageable pageable, JwtAuthenticationToken jwtToken) {
	checkAdmin(jwtToken);
	Page<User> page = usersRepository.findAll(UsersSpec.getSpec(request), pageable);
	return pageMapper.pageToPageResponse(page);
    }

    @Override
    @Transactional
    public String blocking(String username, JwtAuthenticationToken jwtToken) {
	checkAdmin(jwtToken);
	User user = usersService.getUserForUpdate(username);
	boolean updateBlocked = !user.getIsBlocked();
	EditUserRequest editRequest = EditUserRequest.builder().enabled(!updateBlocked).build();
	ssoClient.editUser(username, editRequest);
	user.setIsBlocked(updateBlocked);
	usersRepository.save(user);
	return updateBlocked ? BLOCKED : UNBLOCKED;
    }

    @Override
    @Transactional
    public String changeAdminRole(String username, JwtAuthenticationToken jwtToken) {
	checkAdmin(jwtToken);
	User user = usersService.getUserForUpdate(username);
	AppUtil.checkUserBlocked(user);
	String action = user.getIsAdmin() ? RM : ADD;

	ChangeRoleRequest roleRequest = ChangeRoleRequest.builder()
	        .username(username)
	        .action(action)
	        .roles(List.of(ADMIN.name()))
	        .build();

	ssoClient.changeRole(roleRequest);
	user.setIsAdmin(action.equals(ADD) ? true : false);
	usersRepository.save(user);
	return CHANGED;
    }

    private void checkAdmin(JwtAuthenticationToken jwtToken) {
	String username = tokenMapper.jwtToModel(jwtToken).username();

	User currentUser = usersRepository.findByUsername(username).orElseThrow(() -> {
	    log.warn("User with username {} not found", username);
	    return new ApplicationException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
	});

	if (!currentUser.getIsAdmin() || currentUser.getIsBlocked())
	    throw new ApplicationException("Access denied", HttpStatus.FORBIDDEN);
    }

}
