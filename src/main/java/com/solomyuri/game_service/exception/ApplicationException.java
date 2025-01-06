package com.solomyuri.game_service.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

	private final HttpStatus status;

	public ApplicationException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

}
