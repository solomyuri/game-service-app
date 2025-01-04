package com.solomyuri.game_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.solomyuri.game_service.model.entity.Cell;

@Repository
public interface CellsRepository extends JpaRepository<Cell, UUID> {
}
