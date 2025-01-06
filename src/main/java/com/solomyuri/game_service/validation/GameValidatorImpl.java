package com.solomyuri.game_service.validation;

import com.solomyuri.game_service.enums.ShipType;
import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.model.dto.CellDto;
import com.solomyuri.game_service.model.dto.GameDto;
import com.solomyuri.game_service.model.dto.ShipDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GameValidatorImpl implements GameValidator {

    public void validateGameRequest(GameDto game) {
        Set<ShipDto> ships = game.getShips();
        ships.stream().collect(Collectors.groupingBy(ShipDto::getType)).forEach(this::validateShipCount);
        Set<CellDto> allCells = new HashSet<>();

        ships.forEach(ship -> {
            validateShipCells(ship);
            validateNoOverlap(allCells, ship.getCells());
            allCells.addAll(ship.getCells());
        });
    }

    private void validateShipCount(ShipType type, List<ShipDto> ships) {
        if (ships.size() != type.getShipsCount())
            invokeThrow(String.format(
                    "ship type %s must have exactly %d ships, but found %d.",
                    type.name(), type.getShipsCount(), ships.size()
            ));
    }

    private void validateShipCells(ShipDto ship) {

        if (ship.getCells().size() != ship.getType().getCellsCount())
            invokeThrow(String.format(
                    "ship of type %s must have exactly %d cells, but found %d.",
                    ship.getType(), ship.getType().getCellsCount(), ship.getCells().size()
            ));

        validateCellAdjacency(ship.getCells());
    }

    private void validateCellAdjacency(Set<CellDto> cells) {
        CellDto prev = null;

        List<CellDto> sortedCells = cells.stream()
                .sorted(Comparator.comparing(CellDto::getX).thenComparing(CellDto::getY))
                .toList();

        for (CellDto cell : sortedCells) {
            if (prev != null && !areCellsAdjacent(prev, cell))
                invokeThrow(String.format(
                        "cells %s and %s are not adjacent.",
                        prev.getX() + prev.getY(), cell.getX() + cell.getY()
                ));
            prev = cell;
        }
    }

    private boolean areCellsAdjacent(CellDto c1, CellDto c2) {
        int xDiff = Math.abs(c1.getX().charAt(0) - c2.getX().charAt(0));
        int yDiff = Math.abs(Integer.parseInt(c1.getY()) - Integer.parseInt(c2.getY()));
        return (xDiff == 1 && yDiff == 0) || (xDiff == 0 && yDiff == 1);
    }

    private void validateNoOverlap(Set<CellDto> existingCells, Set<CellDto> newCells) {
        newCells.forEach(newCell -> {

            if (existingCells.contains(newCell))
                invokeThrow(String.format("cell %s is overlapping with another ship.", newCell.getX() + newCell.getY()));

            existingCells.forEach(existingCell -> {
                int xDiff = Math.abs(newCell.getX().charAt(0) - existingCell.getX().charAt(0));
                int yDiff = Math.abs(Integer.parseInt(newCell.getY()) - Integer.parseInt(existingCell.getY()));

                if ((xDiff == 1 && yDiff == 1) || areCellsAdjacent(existingCell, newCell))
                    invokeThrow(String.format("cell %s is adjacent to another ship.", newCell.getX() + newCell.getY()));
            });
        });
    }

    private void invokeThrow(String message) {
        throw new ApplicationException(message, HttpStatus.BAD_REQUEST);
    }
}
