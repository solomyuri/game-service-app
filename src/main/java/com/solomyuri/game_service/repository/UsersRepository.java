package com.solomyuri.game_service.repository;

import com.solomyuri.game_service.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<User, UUID> {

	@EntityGraph(attributePaths = "currentGame")
	Optional<User> findByUsername(String username);

	@EntityGraph(attributePaths = "currentGame")
	Page<User> findAll(Pageable pageable);
}
