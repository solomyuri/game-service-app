package com.solomyuri.game_service.repository;

import com.solomyuri.game_service.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, String> {

	Optional<User> findByUsername(String username);
}
