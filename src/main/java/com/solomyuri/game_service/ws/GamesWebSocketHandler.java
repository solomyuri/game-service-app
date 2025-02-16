package com.solomyuri.game_service.ws;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solomyuri.game_service.mapper.GameMapper;
import com.solomyuri.game_service.model.dto.CellDto;
import com.solomyuri.game_service.model.dto.ErrorResponse;
import com.solomyuri.game_service.model.dto.GameFullDto;
import com.solomyuri.game_service.model.dto.response.GameResponse;
import com.solomyuri.game_service.model.dto.response.ShotWsResponse;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.service.interfaces.GamesService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class GamesWebSocketHandler extends TextWebSocketHandler {

    private final GamesService gameService;
    private final GameMapper gameMapper;
    private final Validator validator;
    private final Map<UUID, Game> activeGames = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String WINNER = "WINNER";
    private final ErrorResponse forbiddenResponse = ErrorResponse.builder()
            .errorCode(403)
            .errorDescription(HttpStatus.FORBIDDEN.getReasonPhrase())
            .build();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
	UUID gameId = gameIdFromSession(session);
	JwtAuthenticationToken jwt = (JwtAuthenticationToken) session.getPrincipal();
	Game game = activeGames.computeIfAbsent(gameId, k -> gameService.getFullGame(gameId, jwt));
	checkGameOwner(game.getOwner().getUsername(), jwt, session);
	GameFullDto gameFullDto = gameMapper.gameToFullDto(game);
	GameResponse response = GameResponse.createResponse(gameFullDto);
	session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
	ShotWsResponse shotResponse = new ShotWsResponse();
	shotMachine(game, shotResponse, session);
	if (!shotResponse.getEnemyShots().isEmpty())
	    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(shotResponse)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
	String payload = message.getPayload();
	CellDto cellDto = objectMapper.readValue(payload, CellDto.class);

	if (!isValidMessage(cellDto, session))
	    return;

	UUID gameId = gameIdFromSession(session);
	Game game = activeGames.get(gameId);
	ShotWsResponse shotResponse = new ShotWsResponse();
	gameService.invokeShotsByUser(game, cellDto, shotResponse);

	if (gameService.isWinner(game, Optional.of(game.getOwner()))) {
	    shotResponse.setGameOver(new ShotWsResponse.GameOver(true));
	    session.getAttributes().put(WINNER, Optional.of(game.getOwner()));
	    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(shotResponse)));
	    session.close();
	} else {
	    shotMachine(game, shotResponse, session);
	    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(shotResponse)));
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
	UUID gameId = gameIdFromSession(session);
	Game game = activeGames.get(gameId);
	Optional<User> winner = (Optional<User>) session.getAttributes().get(WINNER);

	if (Objects.nonNull(winner)) {
	    gameService.gameFinishing(game, winner);
	    activeGames.remove(gameId);
	}
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
	log.error(exception.getMessage(), exception);
	session.close(CloseStatus.SERVER_ERROR);
    }

    private UUID gameIdFromSession(WebSocketSession session) {
	String path = session.getUri().getPath();
	return UUID.fromString(path.substring(path.lastIndexOf("/") + 1));
    }

    @SneakyThrows
    private void shotMachine(Game game, ShotWsResponse shotResponse, WebSocketSession session) {
	if (Objects.isNull(game.getCurrentShooter())) {
	    gameService.invokeShotsByMachine(game, shotResponse);

	    if (gameService.isWinner(game, Optional.empty())) {
		shotResponse.setGameOver(new ShotWsResponse.GameOver(false));
		session.getAttributes().put(WINNER, Optional.empty());
		session.sendMessage(new TextMessage(objectMapper.writeValueAsString(shotResponse)));
		session.close();
	    }
	}
    }

    @SneakyThrows
    private boolean isValidMessage(CellDto cellDto, WebSocketSession session) {
	Set<ConstraintViolation<CellDto>> violations = validator.validate(cellDto);
	if (!violations.isEmpty()) {
	    String message = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "));
	    ErrorResponse response = ErrorResponse.builder().errorCode(400).errorDescription(message).build();
	    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
	    return false;
	}
	return true;
    }

    @SneakyThrows
    private void checkGameOwner(String owner, JwtAuthenticationToken token, WebSocketSession session) {
	String username = (String) token.getToken().getClaims().get("preferred_username");
	if (owner.equals(username))
	    return;
	else {
	    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(forbiddenResponse)));
	    session.close(CloseStatus.POLICY_VIOLATION);
	}
    }

}
