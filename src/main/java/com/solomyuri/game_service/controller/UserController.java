package com.solomyuri.game_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solomyuri.game_service.model.dto.UserDto;
import com.solomyuri.game_service.service.UsersService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("v1")
@Slf4j
@RequiredArgsConstructor
public class UserController {
	
	private final UsersService usersService;

	@GetMapping("home")
	public ResponseEntity<UserDto> home(JwtAuthenticationToken token) {

		return ResponseEntity.ok(usersService.getUserFromToken(token));
	}
}
