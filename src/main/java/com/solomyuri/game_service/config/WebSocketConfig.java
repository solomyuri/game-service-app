package com.solomyuri.game_service.config;

import com.solomyuri.game_service.interceptors.AuthHandshakeInterceptor;
import com.solomyuri.game_service.ws.GamesWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GamesWebSocketHandler gameWebSocketHandler;

    public WebSocketConfig(GamesWebSocketHandler gameWebSocketHandler) {
	this.gameWebSocketHandler = gameWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
	registry.addHandler(gameWebSocketHandler, "/v1/battleship/{gameId}")
	        .addInterceptors(new AuthHandshakeInterceptor())
	        .setAllowedOrigins("*");
    }
}