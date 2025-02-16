package com.solomyuri.game_service.service;

import static com.solomyuri.game_service.enums.ShotResult.DESTROY;
import static com.solomyuri.game_service.enums.ShotResult.MISS;
import static com.solomyuri.game_service.enums.ShotResult.STRIKE;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomyuri.game_service.enums.ShotResult;
import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.mapper.CellMapper;
import com.solomyuri.game_service.mapper.GameMapper;
import com.solomyuri.game_service.mapper.ShotMapper;
import com.solomyuri.game_service.model.dto.CellDto;
import com.solomyuri.game_service.model.dto.CellFullDto;
import com.solomyuri.game_service.model.dto.request.CreateGameRequest;
import com.solomyuri.game_service.model.dto.response.CreateGameResponse;
import com.solomyuri.game_service.model.dto.response.ShotWsResponse;
import com.solomyuri.game_service.model.entity.Cell;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.Ship;
import com.solomyuri.game_service.model.entity.Shot;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.CellsRepository;
import com.solomyuri.game_service.repository.GamesRepository;
import com.solomyuri.game_service.repository.ShipsRepository;
import com.solomyuri.game_service.repository.ShotsRepository;
import com.solomyuri.game_service.repository.UsersRepository;
import com.solomyuri.game_service.service.interfaces.GamesService;
import com.solomyuri.game_service.service.interfaces.ShipsGenerator;
import com.solomyuri.game_service.util.AppUtil;
import com.solomyuri.game_service.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GamesServiceImpl implements GamesService {

    private final UsersRepository usersRepository;
    private final GamesRepository gamesRepository;
    private final CellsRepository cellsRepository;
    private final ShipsRepository shipsRepository;
    private final ShotsRepository shotsRepository;
    private final ShipsGenerator shipsGenerator;
    private final GameMapper gameMapper;
    private final ShotMapper shotMapper;
    private final CellMapper cellMapper;

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
	gamesRepository.findWithShots(gameId);

	return game;
    }

    @Override
    @Transactional
    public void invokeShotsByMachine(Game game, ShotWsResponse response) {
	Map<String, Cell> openedCells = new HashMap<>();
	Map<String, Cell> shotCells = new HashMap<>();
	Map<String, Cell> closedCells = new HashMap<>();
	List<Shot> shots = getShots(game, Optional.empty());
	extractUserCells(game, openedCells, shotCells, closedCells);
	boolean isHasStrike = isHasStrike(shots);
	int shotNumber = getShotNumber(shots);

	while (true) {
	    Cell targetCell;

	    if (isHasStrike)
		targetCell = findNextTargetAfterStrike(shots, closedCells, shotCells);
	    else
		targetCell = AppUtil.getRandomByAvailable(closedCells);

	    Shot currentShot = createAndSetShot(game, targetCell, shotNumber++, null);
	    shots.add(0, currentShot);
	    String targetCoordinate = targetCell.getX() + targetCell.getY();
	    response.getUserCellsOpen().add(cellMapper.entityToFullDto(targetCell));
	    response.getEnemyShots().add(shotMapper.entityToDto(currentShot));
	    shotCells.put(targetCoordinate, targetCell);
	    openedCells.put(targetCoordinate, targetCell);
	    closedCells.remove(targetCoordinate);
	    cellsRepository.updateForOpen(targetCell.getId());
		
	    switch (currentShot.getResult()) {
	    case DESTROY -> {
		isHasStrike = false;
		handleDestroy(targetCell.getShip(), closedCells, openedCells, response.getUserCellsOpen());
	    }
	    case STRIKE -> {
		isHasStrike = true;
	    }
	    default -> {
		game.setCurrentShooter(game.getOwner());
		gamesRepository.updateShooter(game.getId(), game.getCurrentShooter());
		return;
	    }
	    }
	}
    }

    @Override
    @Transactional
    public void invokeShotsByUser(Game game, CellDto targetCellDto, ShotWsResponse response) {

	Cell targetCell = game.getCells()
	        .stream()
	        .filter(cell -> Objects.isNull(cell.getUser()) &&
	                cell.getX().equals(targetCellDto.getX()) &&
	                cell.getY().equals(targetCellDto.getY()))
	        .findFirst()
	        .orElseThrow(() -> new ApplicationException("Unknown target cell", HttpStatus.BAD_REQUEST));

	Map<String, Cell> openedCells = new HashMap<>();
	Map<String, Cell> closedCells = new HashMap<>();
	extractMachineCells(game, openedCells, closedCells);

	User user = game.getOwner();
	List<Shot> shots = getShots(game, Optional.of(user));
	int shotNumber = getShotNumber(shots);
	Shot shot = createAndSetShot(game, targetCell, shotNumber, user);

	response.getEnemyCellsOpen().add(cellMapper.entityToFullDto(targetCell));
	response.setUserShot(shotMapper.entityToDto(shot));
	cellsRepository.updateForOpen(targetCell.getId());

	if (shot.getResult() == DESTROY) {
	    handleDestroy(shot.getCell().getShip(), closedCells, openedCells, response.getEnemyCellsOpen());
	} else if (shot.getResult() == MISS) {
	    game.setCurrentShooter(null);
	    gamesRepository.updateShooter(game.getId(), game.getCurrentShooter());
	}
    }

    @Override
    public boolean isWinner(Game game, Optional<User> userOpt) {
	return userOpt.map(user -> game.getShips()
	        .stream()
	        .filter(ship -> Objects.isNull(ship.getUser()))
	        .flatMap(ship -> ship.getCells().stream())
	        .allMatch(Cell::getIsOpen))
	        .orElseGet(() -> game.getShips()
	                .stream()
	                .filter(ship -> Objects.nonNull(ship.getUser()))
	                .flatMap(ship -> ship.getCells().stream())
	                .allMatch(Cell::getIsOpen));
    }

    @Override
    public void gameFinishing(Game game, Optional<User> winner) {
	winner.ifPresentOrElse(user -> user.setWinCount(user.getWinCount() + 1),
	        () -> game.getOwner().setLoseCount(game.getOwner().getLoseCount() + 1));
	usersRepository.save(game.getOwner());
	gamesRepository.deleteById(game.getId());
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

    private List<Shot> getShots(Game game, Optional<User> userOpt) {
	return userOpt.map(user -> game.getShots()
	        .stream()
	        .filter(shot -> user.equals(shot.getUser()))
	        .sorted(Comparator.comparingInt(Shot::getNumber).reversed())
	        .collect(Collectors.toList()))
	        .orElseGet(() -> game.getShots()
	                .stream()
	                .filter(shot -> Objects.isNull(shot.getUser()))
	                .sorted(Comparator.comparingInt(Shot::getNumber).reversed())
	                .collect(Collectors.toList()));
    }

    private void extractUserCells(Game game, Map<String, Cell> openedCells,
                                  Map<String, Cell> shotCells, Map<String, Cell> closedCells) {
	game.getCells()
	        .stream()
	        .filter(cell -> Objects.nonNull(cell.getUser()))
	        .forEach(cell -> {
	            if (!cell.getIsOpen())
		        closedCells.put(cell.getCoordinate(), cell);
	            else {
		        openedCells.put(cell.getCoordinate(), cell);
		        if (Objects.nonNull(cell.getShot()))
		            shotCells.put(cell.getCoordinate(), cell);
	            }
	        });
    }

    private void extractMachineCells(Game game, Map<String, Cell> openedCells, Map<String, Cell> closedCells) {
	game.getCells()
	        .stream()
	        .filter(cell -> Objects.isNull(cell.getUser()))
	        .forEach(cell -> {
	            if (!cell.getIsOpen())
		        closedCells.put(cell.getCoordinate(), cell);
	            else {
		        openedCells.put(cell.getCoordinate(), cell);
	            }
	        });
    }

    private boolean isHasStrike(List<Shot> shots) {
	AtomicBoolean isHasStrike = new AtomicBoolean(false);
	shots.stream()
	        .map(Shot::getResult)
	        .filter(result -> result.equals(STRIKE) || result.equals(DESTROY))
	        .findFirst()
	        .ifPresent(result -> isHasStrike.set(result.equals(STRIKE)));
	return isHasStrike.get();
    }

    private Cell findNextTargetAfterStrike(List<Shot> shots, Map<String, Cell> closedCells,
                                           Map<String, Cell> shotCells) {

	Shot lastStrike = shots.stream()
	        .filter(shot -> shot.getResult().equals(STRIKE))
	        .findFirst()
	        .orElseThrow();

	Set<String> horizontalCoordinates = getSideHorizontalCoordinates(
	        lastStrike.getCell().getX(), lastStrike.getCell().getY());

	Set<String> verticalCoordinates = getSideVerticalCoordinates(
	        lastStrike.getCell().getX(), lastStrike.getCell().getY());

	boolean isLine = true;
	boolean isHorizontal = false;
	Map<String, Cell> targetCells;

	if (isLine(shotCells, horizontalCoordinates)) {
	    isHorizontal = true;
	} else if (!isLine(shotCells, verticalCoordinates)) {
	    isLine = false;
	}

	if (isLine) {
	    if (isHorizontal) {
		targetCells = horizontalCoordinates.stream()
		        .map(closedCells::get)
		        .filter(Objects::nonNull)
		        .collect(Collectors.toMap(cell -> cell.getX() + cell.getY(), Function.identity()));
		if (targetCells.isEmpty())
		    return findNextHorizontalCell(lastStrike, shotCells, closedCells);
		else
		    return AppUtil.getRandomByAvailable(targetCells);
	    } else {
		targetCells = verticalCoordinates.stream()
		        .map(closedCells::get)
		        .filter(Objects::nonNull)
		        .collect(Collectors.toMap(cell -> cell.getX() + cell.getY(), Function.identity()));
		if (targetCells.isEmpty())
		    return findNextVerticalCell(lastStrike, shotCells, closedCells);
		else
		    return AppUtil.getRandomByAvailable(targetCells);
	    }
	} else {
	    Set<String> targetCoordinates = new HashSet<>();
	    targetCoordinates.addAll(horizontalCoordinates);
	    targetCoordinates.addAll(verticalCoordinates);

	    targetCells = targetCoordinates.stream()
	            .map(closedCells::get)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toMap(cell -> cell.getX() + cell.getY(), Function.identity()));

	    return AppUtil.getRandomByAvailable(targetCells);
	}
    }

    private Set<String> getSideHorizontalCoordinates(String x, String y) {
	int xNumber = Constants.X_TO_NUMBER.get(x);
	Set<String> coordinates = new HashSet<>();
	coordinates.add(Constants.NUMBER_TO_X.get(xNumber + 1) + y);
	coordinates.add(Constants.NUMBER_TO_X.get(xNumber - 1) + y);
	return coordinates;
    }

    private Set<String> getSideVerticalCoordinates(String x, String y) {
	int yNumber = Integer.parseInt(y);
	Set<String> coordinates = new HashSet<>();
	coordinates.add(x + (yNumber + 1));
	coordinates.add(x + (yNumber - 1));
	return coordinates;
    }

    private boolean isLine(Map<String, Cell> shotCells, Set<String> coordinates) {
	return coordinates.stream()
	        .map(shotCells::get)
	        .filter(Objects::nonNull)
	        .map(Cell::getShot)
	        .anyMatch(shot -> shot.getResult().equals(STRIKE));
    }

    private ShotResult getShotResult(Cell cell) {
	if (Objects.isNull(cell.getShip()))
	    return MISS;
	else if (cell.getShip().getCells().stream().allMatch(Cell::getIsOpen))
	    return DESTROY;
	else
	    return STRIKE;
    }

    private Cell findNextHorizontalCell(Shot lastStrike, Map<String, Cell> shotCells, Map<String, Cell> closedCells) {
	String y = lastStrike.getCell().getY();
	int startX = Constants.X_TO_NUMBER.get(lastStrike.getCell().getX());
	int i = 1;
	String target;

	do {
	    target = Constants.NUMBER_TO_X.get(startX - i++) + y;
	} while (shotCells.containsKey(target) || !closedCells.containsKey(target));

	if (!closedCells.containsKey(target)) {
	    i = 1;
	    do {
		target = Constants.NUMBER_TO_X.get(startX + i++) + y;
	    } while (shotCells.containsKey(target));
	}

	return closedCells.get(target);
    }

    private Cell findNextVerticalCell(Shot lastStrike, Map<String, Cell> shotCells, Map<String, Cell> closedCells) {
	String x = lastStrike.getCell().getX();
	int startY = Integer.parseInt(lastStrike.getCell().getY());
	int i = 1;
	String target;

	do {
	    target = x + (startY - i++);
	} while (shotCells.containsKey(target) || !closedCells.containsKey(target));

	if (!closedCells.containsKey(target)) {
	    i = 1;	
	    do {
		target = x + (startY + i++);
	    } while (shotCells.containsKey(target));
	}

	return closedCells.get(target);
    }

    private void handleDestroy(Ship destroyedShip, Map<String, Cell> closedCells,
                               Map<String, Cell> openedCells, Set<CellFullDto> responseCells) {
	Set<String> openedCoordinates = new HashSet<>();
	destroyedShip.getCells()
	        .forEach(cell -> openedCoordinates.addAll(getOpenedCoordinates(cell.getX(), cell.getY())));

	openedCoordinates.forEach(coordinate -> {
	    if (closedCells.containsKey(coordinate)) {
		Cell cell = closedCells.get(coordinate);
		cell.setIsOpen(true);
		openedCells.put(coordinate, cell);
		closedCells.remove(coordinate);
		responseCells.add(cellMapper.entityToFullDto(cell));
		cellsRepository.updateForOpen(cell.getId());
	    }
	});
    }

    private Set<String> getOpenedCoordinates(String targetX, String targetY) {
	Set<String> coordinates = new HashSet<>();
	int x = Constants.X_TO_NUMBER.get(targetX);
	int y = Integer.parseInt(targetY);

	coordinates.add(targetX + (y - 1));
	coordinates.add(targetX + (y + 1));
	coordinates.add(Constants.NUMBER_TO_X.get(x - 1) + y);
	coordinates.add(Constants.NUMBER_TO_X.get(x + 1) + y);
	coordinates.add(Constants.NUMBER_TO_X.get(x - 1) + (y - 1));
	coordinates.add(Constants.NUMBER_TO_X.get(x - 1) + (y + 1));
	coordinates.add(Constants.NUMBER_TO_X.get(x + 1) + (y - 1));
	coordinates.add(Constants.NUMBER_TO_X.get(x + 1) + (y + 1));

	return coordinates;
    }

    private int getShotNumber(List<Shot> shots) {
	if (!shots.isEmpty())
	    return shots.get(0).getNumber() + 1;
	else
	    return 1;
    }

    private Shot createAndSetShot(Game game, Cell targetCell, int shotNumber, User user) {
	targetCell.setIsOpen(true);

	Shot shot = Shot.builder()
	        .game(game)
	        .user(user)
	        .cell(targetCell)
	        .number(shotNumber)
	        .result(getShotResult(targetCell))
	        .build();

	shotsRepository.saveAndFlush(shot);
	game.getShots().add(shot);
	targetCell.setShot(shot);
	return shot;
    }

}
