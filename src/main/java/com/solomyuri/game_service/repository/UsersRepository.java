package com.solomyuri.game_service.repository;

import com.solomyuri.game_service.model.entity.User;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    
    @EntityGraph(attributePaths = "currentGame")
    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsernameWithGame(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.username = :username")
    Optional<User> findForUpdate(String username);

    @EntityGraph(attributePaths = "currentGame")
    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @Modifying
    @Query("delete User u where u.username = :username")
    void deleteUser(String username);
}
