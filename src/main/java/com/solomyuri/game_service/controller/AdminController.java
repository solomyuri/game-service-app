package com.solomyuri.game_service.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solomyuri.game_service.model.dto.PageDto;
import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.model.dto.request.SearchUsersRequest;
import com.solomyuri.game_service.service.interfaces.AdminService;
import com.solomyuri.game_service.util.AppUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("v1/admin")
public class AdminController {
    
    private final AdminService adminService;
 
    @GetMapping("users")
    public ResponseEntity<PageDto<UserDto>> getUsers(@PageableDefault(size = 10,
                                                                      page = 0,
                                                                      sort = "username",
                                                                      direction = Direction.ASC) Pageable pageable,
                                                     SearchUsersRequest request) {
	PageDto<UserDto> page = adminService.getUsers(request, pageable);
	return ResponseEntity.status(AppUtil.getStatusByPage(page)).body(page);
    }
}
