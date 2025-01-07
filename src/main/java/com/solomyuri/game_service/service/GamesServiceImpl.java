package com.solomyuri.game_service.service;

import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.mapper.GameMapper;
import com.solomyuri.game_service.model.dto.request.CreateGameRequest;
import com.solomyuri.game_service.model.dto.response.CreateGameResponse;
import com.solomyuri.game_service.model.entity.Cell;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.CellsRepository;
import com.solomyuri.game_service.repository.GamesRepository;
import com.solomyuri.game_service.repository.ShipsRepository;
import com.solomyuri.game_service.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GamesServiceImpl implements GamesService {

	private final UsersRepository usersRepository;
	private final GamesRepository gamesRepository;
	private final CellsRepository cellsRepository;
	private final ShipsRepository shipsRepository;
	private final ShipsGenerator shipsGenerator;
	private final GameMapper gameMapper;

	@Override
	@Transactional
	public CreateGameResponse createGame(CreateGameRequest request, JwtAuthenticationToken token) {
		User user = getUserFromToken(token);

		if (Objects.nonNull(user.getCurrentGame()))
			gamesRepository.deleteById(user.getCurrentGame().getId());

		Game userGame = gameMapper.dtoToEntity(request.getGame());
		user.setCurrentGame(userGame);
		userGame.setOwner(user);
		userGame.setCurrentShooter(new Random().nextBoolean() ? user : null);
	    gamesRepository.saveAndFlush(userGame);
		Map<String, Cell> cellsMap = new HashMap<>();
		setShipsAndCells(userGame, cellsMap);
		fillCellsMap(cellsMap, user, userGame);
		Map<String, Cell> machineCellsMap = new HashMap<>();
		fillCellsMap(machineCellsMap, null, userGame);
		shipsRepository.saveAll(shipsGenerator.generateShipsSet(machineCellsMap, userGame));
		cellsRepository.saveAll(cellsMap.values());
		cellsRepository.saveAll(machineCellsMap.values());
		return new CreateGameResponse(userGame.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public Game getFullGame(UUID gameId, JwtAuthenticationToken token) {
		User user = getUserFromToken(token);
		checkGameByUser(user, gameId);

		Game game = gamesRepository.findById(gameId).orElseThrow(() -> {
			log.warn("Game with id {} not found", gameId);
			return new ApplicationException("Game not found", HttpStatus.NOT_FOUND);
		});

		gamesRepository.findWithCells(gameId);
		gamesRepository.findWithShips(gameId);

		return game;
	}

	private User getUserFromToken(JwtAuthenticationToken token) {
		String username = (String) token.getToken().getClaims().get("preferred_username");
		return usersRepository.findByUsername(username).orElseThrow(() -> {
			log.warn("User with username {} not found", username);
			return new ApplicationException("User not found", HttpStatus.NOT_FOUND);
		});
	}

	private void checkGameByUser(User user, UUID gameId) {
		if (Objects.isNull(user.getCurrentGame()) || !gameId.equals(user.getCurrentGame().getId())) {
			log.warn("User with username {} have not game with id: {}", user.getUsername(), gameId);
			throw new ApplicationException("Game not found", HttpStatus.NOT_FOUND);
		}
	}

	private void setShipsAndCells(Game game, Map<String, Cell> cellsMap) {
		User user = game.getOwner();

		game.getShips().forEach(ship -> {
			ship.setGame(game);
			ship.setUser(user);
			ship.getCells().forEach(cell -> {
				cell.setShip(ship);
				cell.setGame(game);
				cell.setUser(user);
				cellsMap.put(cell.getX() + cell.getY(), cell);
			});
		});

		shipsRepository.saveAll(game.getShips());
	}
	
	private void fillCellsMap(Map<String, Cell> cells, User user, Game game) {
		cells.computeIfAbsent("A1", k -> Cell.builder().x("A").y("1").user(user).game(game).build());
		cells.computeIfAbsent("A2", k -> Cell.builder().x("A").y("2").user(user).game(game).build());
		cells.computeIfAbsent("A3", k -> Cell.builder().x("A").y("3").user(user).game(game).build());
		cells.computeIfAbsent("A4", k -> Cell.builder().x("A").y("4").user(user).game(game).build());
		cells.computeIfAbsent("A5", k -> Cell.builder().x("A").y("5").user(user).game(game).build());
		cells.computeIfAbsent("A6", k -> Cell.builder().x("A").y("6").user(user).game(game).build());
		cells.computeIfAbsent("A7", k -> Cell.builder().x("A").y("7").user(user).game(game).build());
		cells.computeIfAbsent("A8", k -> Cell.builder().x("A").y("8").user(user).game(game).build());
		cells.computeIfAbsent("A9", k -> Cell.builder().x("A").y("9").user(user).game(game).build());
		cells.computeIfAbsent("A10", k -> Cell.builder().x("A").y("10").user(user).game(game).build());		
		cells.computeIfAbsent("B1", k -> Cell.builder().x("B").y("1").user(user).game(game).build());
		cells.computeIfAbsent("B2", k -> Cell.builder().x("B").y("2").user(user).game(game).build());
		cells.computeIfAbsent("B3", k -> Cell.builder().x("B").y("3").user(user).game(game).build());
		cells.computeIfAbsent("B4", k -> Cell.builder().x("B").y("4").user(user).game(game).build());
		cells.computeIfAbsent("B5", k -> Cell.builder().x("B").y("5").user(user).game(game).build());
		cells.computeIfAbsent("B6", k -> Cell.builder().x("B").y("6").user(user).game(game).build());
		cells.computeIfAbsent("B7", k -> Cell.builder().x("B").y("7").user(user).game(game).build());
		cells.computeIfAbsent("B8", k -> Cell.builder().x("B").y("8").user(user).game(game).build());
		cells.computeIfAbsent("B9", k -> Cell.builder().x("B").y("9").user(user).game(game).build());
		cells.computeIfAbsent("B10", k -> Cell.builder().x("B").y("10").user(user).game(game).build());		
		cells.computeIfAbsent("C1", k -> Cell.builder().x("C").y("1").user(user).game(game).build());
		cells.computeIfAbsent("C2", k -> Cell.builder().x("C").y("2").user(user).game(game).build());
		cells.computeIfAbsent("C3", k -> Cell.builder().x("C").y("3").user(user).game(game).build());
		cells.computeIfAbsent("C4", k -> Cell.builder().x("C").y("4").user(user).game(game).build());
		cells.computeIfAbsent("C5", k -> Cell.builder().x("C").y("5").user(user).game(game).build());
		cells.computeIfAbsent("C6", k -> Cell.builder().x("C").y("6").user(user).game(game).build());
		cells.computeIfAbsent("C7", k -> Cell.builder().x("C").y("7").user(user).game(game).build());
		cells.computeIfAbsent("C8", k -> Cell.builder().x("C").y("8").user(user).game(game).build());
		cells.computeIfAbsent("C9", k -> Cell.builder().x("C").y("9").user(user).game(game).build());
		cells.computeIfAbsent("C10", k -> Cell.builder().x("C").y("10").user(user).game(game).build());		
		cells.computeIfAbsent("D1", k -> Cell.builder().x("D").y("1").user(user).game(game).build());
		cells.computeIfAbsent("D2", k -> Cell.builder().x("D").y("2").user(user).game(game).build());
		cells.computeIfAbsent("D3", k -> Cell.builder().x("D").y("3").user(user).game(game).build());
		cells.computeIfAbsent("D4", k -> Cell.builder().x("D").y("4").user(user).game(game).build());
		cells.computeIfAbsent("D5", k -> Cell.builder().x("D").y("5").user(user).game(game).build());
		cells.computeIfAbsent("D6", k -> Cell.builder().x("D").y("6").user(user).game(game).build());
		cells.computeIfAbsent("D7", k -> Cell.builder().x("D").y("7").user(user).game(game).build());
		cells.computeIfAbsent("D8", k -> Cell.builder().x("D").y("8").user(user).game(game).build());
		cells.computeIfAbsent("D9", k -> Cell.builder().x("D").y("9").user(user).game(game).build());
		cells.computeIfAbsent("D10", k -> Cell.builder().x("D").y("10").user(user).game(game).build());
		cells.computeIfAbsent("E1", k -> Cell.builder().x("E").y("1").user(user).game(game).build());
		cells.computeIfAbsent("E2", k -> Cell.builder().x("E").y("2").user(user).game(game).build());
		cells.computeIfAbsent("E3", k -> Cell.builder().x("E").y("3").user(user).game(game).build());
		cells.computeIfAbsent("E4", k -> Cell.builder().x("E").y("4").user(user).game(game).build());
		cells.computeIfAbsent("E5", k -> Cell.builder().x("E").y("5").user(user).game(game).build());
		cells.computeIfAbsent("E6", k -> Cell.builder().x("E").y("6").user(user).game(game).build());
		cells.computeIfAbsent("E7", k -> Cell.builder().x("E").y("7").user(user).game(game).build());
		cells.computeIfAbsent("E8", k -> Cell.builder().x("E").y("8").user(user).game(game).build());
		cells.computeIfAbsent("E9", k -> Cell.builder().x("E").y("9").user(user).game(game).build());
		cells.computeIfAbsent("E10", k -> Cell.builder().x("E").y("10").user(user).game(game).build());
		cells.computeIfAbsent("F1", k -> Cell.builder().x("F").y("1").user(user).game(game).build());
		cells.computeIfAbsent("F2", k -> Cell.builder().x("F").y("2").user(user).game(game).build());
		cells.computeIfAbsent("F3", k -> Cell.builder().x("F").y("3").user(user).game(game).build());
		cells.computeIfAbsent("F4", k -> Cell.builder().x("F").y("4").user(user).game(game).build());
		cells.computeIfAbsent("F5", k -> Cell.builder().x("F").y("5").user(user).game(game).build());
		cells.computeIfAbsent("F6", k -> Cell.builder().x("F").y("6").user(user).game(game).build());
		cells.computeIfAbsent("F7", k -> Cell.builder().x("F").y("7").user(user).game(game).build());
		cells.computeIfAbsent("F8", k -> Cell.builder().x("F").y("8").user(user).game(game).build());
		cells.computeIfAbsent("F9", k -> Cell.builder().x("F").y("9").user(user).game(game).build());
		cells.computeIfAbsent("F10", k -> Cell.builder().x("F").y("10").user(user).game(game).build());
		cells.computeIfAbsent("G1", k -> Cell.builder().x("G").y("1").user(user).game(game).build());
		cells.computeIfAbsent("G2", k -> Cell.builder().x("G").y("2").user(user).game(game).build());
		cells.computeIfAbsent("G3", k -> Cell.builder().x("G").y("3").user(user).game(game).build());
		cells.computeIfAbsent("G4", k -> Cell.builder().x("G").y("4").user(user).game(game).build());
		cells.computeIfAbsent("G5", k -> Cell.builder().x("G").y("5").user(user).game(game).build());
		cells.computeIfAbsent("G6", k -> Cell.builder().x("G").y("6").user(user).game(game).build());
		cells.computeIfAbsent("G7", k -> Cell.builder().x("G").y("7").user(user).game(game).build());
		cells.computeIfAbsent("G8", k -> Cell.builder().x("G").y("8").user(user).game(game).build());
		cells.computeIfAbsent("G9", k -> Cell.builder().x("G").y("9").user(user).game(game).build());
		cells.computeIfAbsent("G10", k -> Cell.builder().x("G").y("10").user(user).game(game).build());
		cells.computeIfAbsent("H1", k -> Cell.builder().x("H").y("1").user(user).game(game).build());
		cells.computeIfAbsent("H2", k -> Cell.builder().x("H").y("2").user(user).game(game).build());
		cells.computeIfAbsent("H3", k -> Cell.builder().x("H").y("3").user(user).game(game).build());
		cells.computeIfAbsent("H4", k -> Cell.builder().x("H").y("4").user(user).game(game).build());
		cells.computeIfAbsent("H5", k -> Cell.builder().x("H").y("5").user(user).game(game).build());
		cells.computeIfAbsent("H6", k -> Cell.builder().x("H").y("6").user(user).game(game).build());
		cells.computeIfAbsent("H7", k -> Cell.builder().x("H").y("7").user(user).game(game).build());
		cells.computeIfAbsent("H8", k -> Cell.builder().x("H").y("8").user(user).game(game).build());
		cells.computeIfAbsent("H9", k -> Cell.builder().x("H").y("9").user(user).game(game).build());
		cells.computeIfAbsent("H10", k -> Cell.builder().x("H").y("10").user(user).game(game).build());
		cells.computeIfAbsent("I1", k -> Cell.builder().x("I").y("1").user(user).game(game).build());
		cells.computeIfAbsent("I2", k -> Cell.builder().x("I").y("2").user(user).game(game).build());
		cells.computeIfAbsent("I3", k -> Cell.builder().x("I").y("3").user(user).game(game).build());
		cells.computeIfAbsent("I4", k -> Cell.builder().x("I").y("4").user(user).game(game).build());
		cells.computeIfAbsent("I5", k -> Cell.builder().x("I").y("5").user(user).game(game).build());
		cells.computeIfAbsent("I6", k -> Cell.builder().x("I").y("6").user(user).game(game).build());
		cells.computeIfAbsent("I7", k -> Cell.builder().x("I").y("7").user(user).game(game).build());
		cells.computeIfAbsent("I8", k -> Cell.builder().x("I").y("8").user(user).game(game).build());
		cells.computeIfAbsent("I9", k -> Cell.builder().x("I").y("9").user(user).game(game).build());
		cells.computeIfAbsent("I10", k -> Cell.builder().x("I").y("10").user(user).game(game).build());
		cells.computeIfAbsent("J1", k -> Cell.builder().x("J").y("1").user(user).game(game).build());
		cells.computeIfAbsent("J2", k -> Cell.builder().x("J").y("2").user(user).game(game).build());
		cells.computeIfAbsent("J3", k -> Cell.builder().x("J").y("3").user(user).game(game).build());
		cells.computeIfAbsent("J4", k -> Cell.builder().x("J").y("4").user(user).game(game).build());
		cells.computeIfAbsent("J5", k -> Cell.builder().x("J").y("5").user(user).game(game).build());
		cells.computeIfAbsent("J6", k -> Cell.builder().x("J").y("6").user(user).game(game).build());
		cells.computeIfAbsent("J7", k -> Cell.builder().x("J").y("7").user(user).game(game).build());
		cells.computeIfAbsent("J8", k -> Cell.builder().x("J").y("8").user(user).game(game).build());
		cells.computeIfAbsent("J9", k -> Cell.builder().x("J").y("9").user(user).game(game).build());
		cells.computeIfAbsent("J10", k -> Cell.builder().x("J").y("10").user(user).game(game).build());
	}

}
