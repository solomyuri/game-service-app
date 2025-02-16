package com.solomyuri.game_service.repository.specification;

import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import com.solomyuri.game_service.model.dto.request.SearchUsersRequest;
import com.solomyuri.game_service.model.entity.User;

public class UsersSpec {

    private static final String CB_LIKE_TEMPLATE = "%%s%";

    public static Specification<User> getSpec(SearchUsersRequest request) {
	return Specification.where(specByUsername(request.getUsername()).and(specByEmail(request.getEmail())));
    }

    private static Specification<User> specByUsername(String username) {
	return (root, query, cb) -> Objects.isNull(username) || username.isEmpty() ? null
	        : cb.like(cb.lower(root.get("username")), String.format(CB_LIKE_TEMPLATE, username));
    }

    private static Specification<User> specByEmail(String email) {
	return (root, query, cb) -> Objects.isNull(email) || email.isEmpty() ? null
	        : cb.like(cb.lower(root.get("email")), String.format(CB_LIKE_TEMPLATE, email));
    }
}
