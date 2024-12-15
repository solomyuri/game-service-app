//package com.solomyuri.game_service.exception;
//
//import java.util.Map;
//import java.util.Objects;
//
//import org.apache.logging.log4j.util.InternalException;
//import org.springframework.boot.autoconfigure.web.WebProperties;
//import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
//import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
//import org.springframework.boot.web.reactive.error.ErrorAttributes;
//import org.springframework.context.ApplicationContext;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.codec.ServerCodecConfigurer;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.support.WebExchangeBindException;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClientRequestException;
//import org.springframework.web.reactive.function.server.RequestPredicates;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.RouterFunctions;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import org.springframework.web.server.ResponseStatusException;
//
//import com.solomyuri.game_service.model.ErrorResponse;
//
//import jakarta.validation.ConstraintViolationException;
//import jakarta.validation.ValidationException;
//import lombok.extern.slf4j.Slf4j;
//import reactor.core.publisher.Mono;
//
//@Component
//@Slf4j
//public class ExceptionHandler extends AbstractErrorWebExceptionHandler {
//
//	private final Map<Class<? extends Exception>, HttpStatus> exceptionToStatus;
//
//	public ExceptionHandler(WebProperties webProperties, ApplicationContext applicationContext,
//			ServerCodecConfigurer configurer) {
//		super(new DefaultErrorAttributes(), webProperties.getResources(), applicationContext);
//
//		this.setMessageWriters(configurer.getWriters());
//		this.setMessageReaders(configurer.getReaders());
//
//		exceptionToStatus = Map.of(InternalException.class, HttpStatus.INTERNAL_SERVER_ERROR, ValidationException.class,
//				HttpStatus.BAD_REQUEST, ConstraintViolationException.class, HttpStatus.BAD_REQUEST,
//				WebExchangeBindException.class, HttpStatus.BAD_REQUEST, WebClientRequestException.class,
//				HttpStatus.BAD_GATEWAY);
//	}
//
//	@Override
//	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
//
//		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
//	}
//
//	private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
//
//		Throwable error = getError(request);
//		log.error("An error has occurred", error);
//		HttpStatus httpStatus;
//
//		if (error instanceof ResponseStatusException responseStatusException)
//			httpStatus = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
//		else if (error instanceof ApplicationException exception)
//			httpStatus = exception.getStatus();
//		else if (error instanceof Exception exception)
//			httpStatus = exceptionToStatus.getOrDefault(exception.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
//		else
//			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
//
//		String message = Objects.equals(httpStatus, HttpStatus.BAD_GATEWAY) ? "external system error"
//				: error.getMessage();
//
//		return ServerResponse.status(httpStatus)
//				.contentType(MediaType.APPLICATION_JSON)
//				.body(BodyInserters.fromValue(
//						ErrorResponse.builder().errorCode(httpStatus.value()).errorDescription(message).build()));
//	}
//
//}
