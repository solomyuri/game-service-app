package com.solomyuri.game_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.solomyuri.game_service.model.entity.User;

@Repository
public interface UsersRepository extends JpaRepository<User, String> {

	Optional<User> findByUsername(String username);
}
