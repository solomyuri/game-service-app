package com.solomyuri.game_service.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solomyuri.game_service.model.dto.response.HomeResponse;
import com.solomyuri.game_service.service.UsersService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("v1")
public class UsersController {

    private final UsersService usersService;

    @GetMapping("home")
    public ResponseEntity<HomeResponse> home(JwtAuthenticationToken token,
                                             @PageableDefault(sort = "username") Pageable pageable) {

        return ResponseEntity.ok(usersService.getUserByToken(token, pageable));
    }
}
