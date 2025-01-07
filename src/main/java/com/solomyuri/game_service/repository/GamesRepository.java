package com.solomyuri.game_service.repository;

import com.solomyuri.game_service.model.entity.Game;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GamesRepository extends JpaRepository<Game, UUID> {

    @EntityGraph(attributePaths = "cells.shot")
    @Query("select g from Game g where g.id = :gameId")
    Game findWithCells(UUID gameId);

    @EntityGraph(attributePaths = "ships.cells")
    @Query("select g from Game g where g.id = :gameId")
    Game findWithShips(UUID gameId);
}
