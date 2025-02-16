package com.solomyuri.game_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.solomyuri.game_service.mapper.PageMapper;
import com.solomyuri.game_service.model.dto.PageDto;
import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.dto.request.SearchUsersRequest;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.UsersRepository;
import com.solomyuri.game_service.repository.specification.UsersSpec;
import com.solomyuri.game_service.service.interfaces.AdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final UsersRepository usersRepository;
    private final PageMapper mapper;

    @Override
    public PageDto<UserDto> getUsers(SearchUsersRequest request, Pageable pageable) {
	Page<User> page = usersRepository.findAll(UsersSpec.getSpec(request), pageable);
	return mapper.pageToPageResponse(page);
    }

}
