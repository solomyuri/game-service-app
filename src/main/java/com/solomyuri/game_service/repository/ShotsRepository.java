package com.solomyuri.game_service.repository;

import com.solomyuri.game_service.model.entity.Shot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShotsRepository extends JpaRepository<Shot, UUID> {
}
