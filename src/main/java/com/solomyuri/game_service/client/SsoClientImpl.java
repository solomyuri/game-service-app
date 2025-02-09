package com.solomyuri.game_service.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.solomyuri.game_service.config.properties.SsoClientProperties;
import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.model.dto.sso_client.SsoClientError;
import com.solomyuri.game_service.model.dto.sso_client.UserInfoResponse;

import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SsoClientImpl implements SsoClient {

    private final WebClient webClient;
    private final SsoClientProperties ssoProperties;
    private final String CLIENT_ERROR_MESSAGE_TEMPLATE = "{} {} STATUS: {}";
    private final String USERNAME = "username";

    @Override
    public List<UserInfoResponse> getUser(String username) {
	return webClient.get()
	        .uri(uriBuilder -> uriBuilder.path(ssoProperties.getUsersPath())
	                .queryParam(USERNAME, username)
	                .build())
	        .exchangeToMono(response -> {
	            if (response.statusCode().is2xxSuccessful()) {
		        return response.bodyToMono(new ParameterizedTypeReference<List<UserInfoResponse>>() {
		        });
	            } else {
		        return handleErrorResponse(response, HttpMethod.GET.name(), ssoProperties.getUsersPath());
	            }
	        })
	        .block();
    }

    @Override
    public void deleteUser(String username) {
	webClient.delete()
	        .uri(uriBuilder -> uriBuilder.path(ssoProperties.getUsersPath())
	                .queryParam(USERNAME, username)
	                .build())
	        .exchangeToMono(response -> {
	            if (response.statusCode().is2xxSuccessful()) {
		        return Mono.empty();
	            } else {
		        return handleErrorResponse(response, HttpMethod.DELETE.name(), ssoProperties.getUsersPath());
	            }
	        })
	        .onErrorResume(err -> Mono.error(err))
	        .block();
    }

    private <T> Mono<T> handleErrorResponse(ClientResponse response, String method, String path) {
	log.error(CLIENT_ERROR_MESSAGE_TEMPLATE, method, path, response.statusCode().toString());

	HttpStatus status = response.statusCode().is4xxClientError() ? HttpStatus.INTERNAL_SERVER_ERROR
	        : HttpStatus.BAD_GATEWAY;

	return response.bodyToMono(SsoClientError.class)
		.switchIfEmpty(Mono.error(new ApplicationException(status.getReasonPhrase(), status)))
	        .flatMap(error -> Mono.error(new ApplicationException(error.getErrorDescription(), status)));
    }
}
