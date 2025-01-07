package com.solomyuri.game_service.repository;

import com.solomyuri.game_service.model.entity.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CellsRepository extends JpaRepository<Cell, UUID> {
}
