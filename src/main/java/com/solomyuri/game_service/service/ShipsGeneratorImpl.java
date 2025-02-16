package com.solomyuri.game_service.service;

import static com.solomyuri.game_service.util.Constants.AREA_SIZE;
import static com.solomyuri.game_service.util.Constants.NUMBER_TO_X;
import static com.solomyuri.game_service.util.Constants.REMOVED_CELLS;
import static com.solomyuri.game_service.util.Constants.SHIP_CELLS;
import static com.solomyuri.game_service.util.Constants.X_TO_NUMBER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.solomyuri.game_service.enums.ShipType;
import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.model.entity.Cell;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.Ship;
import com.solomyuri.game_service.service.interfaces.ShipsGenerator;
import com.solomyuri.game_service.util.AppUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ShipsGeneratorImpl implements ShipsGenerator {

    @Override
    public Set<Ship> generateShipsSet(Map<String, Cell> cells, Game game) {
        Map<String, Cell> availableCells = new HashMap<>(cells);
        Set<Ship> ships = new HashSet<>();

        for (ShipType type : ShipType.values()) {
            for (int i = 0; i < type.getShipsCount(); i++) {
                log.debug("Старт генерации корабля типа {} номер {}", type, i + 1);
                Ship ship = generateShip(availableCells, i, type, game);
                ships.add(ship);
            }
        }

        return ships;
    }

    private Ship generateShip(Map<String, Cell> availableCells, int number, ShipType type, Game game) {
        Optional<Ship> shipOptional = Optional.empty();
        Ship ship = Ship.builder().number(number).type(type).game(game).build();
        int attempts = 0;

        while (shipOptional.isEmpty() && attempts < 100) {

            Cell firstCell = AppUtil.getRandomByAvailable(availableCells);
            boolean isHorizontal = AppUtil.getRandomBoolean();
            int front = isHorizontal ? X_TO_NUMBER.get(firstCell.getX()) : Integer.parseInt(firstCell.getY());
            int side = isHorizontal ? Integer.parseInt(firstCell.getY()) : X_TO_NUMBER.get(firstCell.getX());
            log.debug("isHorizontal: {}, front: {}, side: {}", isHorizontal, front, side);
            Map<String, Set<String>> candidates = getCellCandidates(type.getCellsCount(), front, side, isHorizontal);

            if (!candidates.isEmpty() && availableCells.keySet().containsAll(candidates.get(SHIP_CELLS))) {
                log.debug("Удаляем координаты из доступных {}", candidates);
                candidates.get(SHIP_CELLS).forEach(coordinates -> {
                    Cell cell = availableCells.get(coordinates);
                    cell.setGame(game);
                    cell.setShip(ship);
                    ship.getCells().add(cell);
                });
                candidates.get(REMOVED_CELLS).forEach(availableCells::remove);
                shipOptional = Optional.of(ship);
            }
            attempts++;
        }

        return shipOptional.orElseGet(() -> {
            log.error("Failed to place ship {}", type);
            throw new ApplicationException("Failed to deploy fleet", HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    private Map<String, Set<String>> getCellCandidates(int shipCellsCount, int front, int side, boolean isHorizontal) {
        Map<String, Set<String>> candidates = new HashMap<>();
        int freeFrontCount = getMainCellsCount(shipCellsCount, front);

        if (freeFrontCount == 0)
            return candidates;

        Set<String> frontCandidates = new HashSet<>();
        Set<String> frontShipCandidates = new HashSet<>();

        for (int i = 0; i < freeFrontCount; i++) {
            if (i < shipCellsCount)
                frontShipCandidates.add(convertToShipCoordinate(front, i, isHorizontal));
            frontCandidates.add(convertToFrontCoordinate(front, i, isHorizontal));
        }

        int freeSideCount = getSideCellsCount(side);
        Set<String> removedCandidates = new HashSet<>();
        Set<String> shipCandidates = new HashSet<>();

        for (int i = 0; i < freeSideCount; i++) {
            String sideCoordinate = convertToSideCoordinate(side, i, isHorizontal);
            frontCandidates.forEach(coordinate ->
                    removedCandidates.add(convertToRemoved(coordinate, sideCoordinate, isHorizontal)));
        }
        frontShipCandidates.forEach(coordinate ->
                shipCandidates.add(convertToCoordinate(coordinate, side, isHorizontal)));

        candidates.put(SHIP_CELLS, shipCandidates);
        candidates.put(REMOVED_CELLS, removedCandidates);
        return candidates;
    }

    private int getMainCellsCount(int shipCellsCount, int coordinate) {
        int diff = AREA_SIZE - coordinate - shipCellsCount;
        if (diff < 0)
            return 0;
        else if (diff == 0 || coordinate == 1)
            return shipCellsCount + 1;
        else
            return shipCellsCount + 2;
    }

    private int getSideCellsCount(int coordinate) {
        return coordinate == 1 || coordinate == 10 ? 2 : 3;
    }

    private String convertToShipCoordinate(int coordinate, int i, boolean isHorizontal) {
        return isHorizontal ? NUMBER_TO_X.get(coordinate + i) : String.valueOf(coordinate + i);
    }

    private String convertToFrontCoordinate(int front, int i, boolean isHorizontal) {
        int coordinate;
        if (front == 1)
            coordinate = front + i;
        else
            coordinate = front + i - 1;
        return isHorizontal ? NUMBER_TO_X.get(coordinate) : String.valueOf(coordinate);
    }

    private String convertToSideCoordinate(int side, int i, boolean isHorizontal) {
        int coordinate;
        if (side == 1)
            coordinate = side + i;
        else
            coordinate = side + i - 1;
        return isHorizontal ? String.valueOf(coordinate) : NUMBER_TO_X.get(coordinate);
    }

    private String convertToCoordinate(String front, int side, boolean isHorizontal) {
        return isHorizontal ? front + side : NUMBER_TO_X.get(side) + front;
    }

    private String convertToRemoved(String front, String side, boolean isHorizontal) {
        return isHorizontal ? front + side : side + front;
    }

}
