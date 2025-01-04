package com.solomyuri.game_service.exception;

import com.solomyuri.game_service.model.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.Objects;

@Component
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	private final Map<Class<? extends Exception>, HttpStatus> exceptionToStatus;

	public GlobalExceptionHandler() {
		exceptionToStatus = Map.of(
				InternalException.class, HttpStatus.INTERNAL_SERVER_ERROR,
				ValidationException.class, HttpStatus.BAD_REQUEST,
				ConstraintViolationException.class, HttpStatus.BAD_REQUEST,
				BindException.class, HttpStatus.BAD_REQUEST,
				PropertyReferenceException.class, HttpStatus.BAD_REQUEST,
				WebClientRequestException.class, HttpStatus.BAD_GATEWAY,
				NoResourceFoundException.class, HttpStatus.NOT_FOUND
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception exception) {
		log.error("An error has occurred", exception);

		HttpStatus httpStatus;

		if (exception instanceof ResponseStatusException responseStatusException) {
			httpStatus = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
		} else if (exception instanceof ApplicationException applicationException) {
			httpStatus = applicationException.getStatus();
		} else {
			httpStatus = exceptionToStatus.getOrDefault(exception.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		String message = Objects.equals(httpStatus, HttpStatus.BAD_GATEWAY)
				? "External system error"
				: exception.getMessage();

		ErrorResponse errorResponse = ErrorResponse.builder()
				.errorCode(httpStatus.value())
				.errorDescription(message)
				.build();

		return ResponseEntity.status(httpStatus)
				.contentType(MediaType.APPLICATION_JSON)
				.body(errorResponse);
	}

}
