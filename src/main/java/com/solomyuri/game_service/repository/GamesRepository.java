package com.solomyuri.game_service.repository;

import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @EntityGraph(attributePaths = "shots")
    @Query("select g from Game g where g.id = :gameId")
    Game findWithShots(UUID gameId);
    
    @Modifying
    @Query("update Game g set g.currentShooter = :shooter, g.lastUpdatedDate = CURRENT_TIMESTAMP where g.id = :gameId")
    void updateShooter(UUID gameId, User shooter);
}
