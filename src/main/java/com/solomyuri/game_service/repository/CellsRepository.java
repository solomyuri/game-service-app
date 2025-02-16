package com.solomyuri.game_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.solomyuri.game_service.model.entity.Cell;

@Repository
public interface CellsRepository extends JpaRepository<Cell, UUID> {

    @Modifying
    @Query("update Cell c set c.isOpen = true, c.lastUpdatedDate = CURRENT_TIMESTAMP where c.id = :cellId")
    void updateForOpen(UUID cellId);
}
