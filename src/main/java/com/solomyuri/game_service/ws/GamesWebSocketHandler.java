package com.solomyuri.game_service.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solomyuri.game_service.mapper.GameMapper;
import com.solomyuri.game_service.model.dto.CellDto;
import com.solomyuri.game_service.model.dto.GameFullDto;
import com.solomyuri.game_service.model.dto.response.GameResponse;
import com.solomyuri.game_service.model.dto.response.ShotWsResponse;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.service.GamesService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class GamesWebSocketHandler extends TextWebSocketHandler {

    private final Map<UUID, Game> activeGames = new ConcurrentHashMap<>();
    private final GamesService gameService;
    private final GameMapper gameMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String WINNER = "WINNER";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID gameId = gameIdFromSession(session);

        Game game = activeGames.computeIfAbsent(gameId, k -> {
            JwtAuthenticationToken jwt = (JwtAuthenticationToken) session.getPrincipal();
            return gameService.getFullGame(gameId, jwt);
        });

        GameFullDto gameFullDto = gameMapper.gameToFullDto(game);
        GameResponse response = GameResponse.createResponse(gameFullDto);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        shotMachine(game, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        CellDto cellDto = objectMapper.readValue(payload, CellDto.class);
        UUID gameId = gameIdFromSession(session);
        Game game = activeGames.get(gameId);
        ShotWsResponse shotResponse = new ShotWsResponse();
        gameService.invokeShotsByUser(game, cellDto, shotResponse);

        if (gameService.isWinner(game, Optional.of(game.getOwner()))) {
            shotResponse.setGameOver(new ShotWsResponse.GameOver(true));
            session.getAttributes().put(WINNER, Optional.empty());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(shotResponse)));
            session.close();
        } else {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(shotResponse)));
            shotMachine(game, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID gameId = gameIdFromSession(session);
        Game game = activeGames.get(gameId);
        Optional<User> winner = (Optional<User>) session.getAttributes().get(WINNER);
        if (Objects.nonNull(winner))
            gameService.gameFinishing(game, winner);
        activeGames.remove(gameId);
    }

    private UUID gameIdFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        return UUID.fromString(path.substring(path.lastIndexOf("/") + 1));
    }

    @SneakyThrows
    private void shotMachine(Game game, WebSocketSession session) {
        if (Objects.isNull(game.getCurrentShooter())) {
            ShotWsResponse shotResponse = new ShotWsResponse();
            gameService.invokeShotsByMachine(game, shotResponse);

            if (gameService.isWinner(game, Optional.empty())) {
                shotResponse.setGameOver(new ShotWsResponse.GameOver(false));
                session.getAttributes().put(WINNER, Optional.empty());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(shotResponse)));
                session.close();
            } else {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(shotResponse)));
            }
        }
    }

}

