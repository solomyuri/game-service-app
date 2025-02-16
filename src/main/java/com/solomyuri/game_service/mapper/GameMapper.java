package com.solomyuri.game_service.mapper;

import com.solomyuri.game_service.model.dto.CellFullDto;
import com.solomyuri.game_service.model.dto.GameDto;
import com.solomyuri.game_service.model.dto.GameFullDto;
import com.solomyuri.game_service.model.dto.ShipDto;
import com.solomyuri.game_service.model.entity.Cell;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.Ship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface GameMapper {

    ShipMapper SHIP_MAPPER = Mappers.getMapper(ShipMapper.class);
    CellMapper CELL_MAPPER = Mappers.getMapper(CellMapper.class);

    Game dtoToEntity(GameDto dto);

    @Mapping(target = "userShips", expression = "java(filterUserShips(game.getShips()))")
    @Mapping(target = "enemyShipsOpened", expression = "java(filterEnemyShipsOpened(game.getShips()))")
    @Mapping(target = "userCellsOpened", expression = "java(filterUserCellsOpened(game.getCells()))")
    @Mapping(target = "enemyCellsOpened", expression = "java(filterEnemyCellsOpened(game.getCells()))")
    @Mapping(target = "userTurn", expression = "java(game.getCurrentShooter() != null)")
    GameFullDto gameToFullDto(Game game);

    default Set<ShipDto> filterUserShips(Set<Ship> ships) {
	return ships.stream()
	        .filter(ship -> Objects.nonNull(ship.getUser()))
	        .map(SHIP_MAPPER::entityToDto)
	        .collect(Collectors.toSet());
    }

    default Set<ShipDto> filterEnemyShipsOpened(Set<Ship> ships) {
	return ships.stream()
	        .filter(ship -> Objects.isNull(ship.getUser()) && ship.getCells().stream().allMatch(Cell::getIsOpen))
	        .map(SHIP_MAPPER::entityToDto)
	        .collect(Collectors.toSet());
    }

    default Set<CellFullDto> filterUserCellsOpened(Set<Cell> cells) {
	return cells.stream()
	        .filter(cell -> Objects.nonNull(cell.getUser()) && cell.getIsOpen())
	        .map(CELL_MAPPER::entityToFullDto)
	        .collect(Collectors.toSet());
    }

    default Set<CellFullDto> filterEnemyCellsOpened(Set<Cell> cells) {
	return cells.stream()
	        .filter(cell -> Objects.isNull(cell.getUser()) && cell.getIsOpen())
	        .map(CELL_MAPPER::entityToFullDto)
	        .collect(Collectors.toSet());
    }

}
