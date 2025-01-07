package com.solomyuri.game_service.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solomyuri.game_service.enums.ShotResult;
import com.solomyuri.game_service.mapper.CellMapper;
import com.solomyuri.game_service.mapper.GameMapper;
import com.solomyuri.game_service.mapper.ShotMapper;
import com.solomyuri.game_service.model.dto.CellFullDto;
import com.solomyuri.game_service.model.dto.GameFullDto;
import com.solomyuri.game_service.model.dto.ShotDto;
import com.solomyuri.game_service.model.dto.response.GameResponse;
import com.solomyuri.game_service.model.dto.response.ShotWsResponse;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.Shot;
import com.solomyuri.game_service.service.GamesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class GamesWebSocketHandler extends TextWebSocketHandler {

    private final Map<UUID, Game> activeGames = new ConcurrentHashMap<>();
    private final GamesService gameService;
    private final GameMapper gameMapper;
    private final ShotMapper shotMapper;
    private final CellMapper cellMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();


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

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();
        ShotDto shot = objectMapper.readValue(payload, ShotDto.class);
        UUID gameId = gameIdFromSession(session);
        Game game = activeGames.get(gameId);
        Shot shotEntity = new Shot();
        shotEntity.setGame(game);
        shotEntity.setUser(game.getOwner());
        AtomicReference<CellFullDto> cellResponse = new AtomicReference<>();
        game.getCells().stream()
                .filter(cell -> cell.getX().equals(shot.getCell().getX()) && cell.getY().equals(shot.getCell().getY()))
                .findFirst().ifPresent(cell -> {
                    shotEntity.setCell(cell);
                    cellResponse.set(cellMapper.entityToFullDto(cell));
                    cell.setIsOpen(true);
                    if(cell.getShip() == null)
                        shotEntity.setResult(ShotResult.MISS);
                     else
                        shotEntity.setResult(ShotResult.STRIKE);
                });

        ShotWsResponse wsResponse = new ShotWsResponse();
        game.setCurrentShooter(game.getCurrentShooter() == null ? game.getOwner() : null);
        wsResponse.setUserShot(shotMapper.entityToDto(shotEntity));
        wsResponse.setEnemyCellsOpen(Set.of(cellResponse.get()));

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(wsResponse)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID gameId = gameIdFromSession(session);
        activeGames.remove(gameId);
    }

    private UUID gameIdFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        return UUID.fromString(path.substring(path.lastIndexOf("/") + 1));
    }
}

