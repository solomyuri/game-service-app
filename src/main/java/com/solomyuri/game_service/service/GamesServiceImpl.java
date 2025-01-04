package com.solomyuri.game_service.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomyuri.game_service.exception.ApplicationException;
import com.solomyuri.game_service.mapper.GameMapper;
import com.solomyuri.game_service.model.dto.request.CreateGameRequest;
import com.solomyuri.game_service.model.dto.response.CreateGameResponse;
import com.solomyuri.game_service.model.entity.Game;
import com.solomyuri.game_service.model.entity.User;
import com.solomyuri.game_service.repository.GamesRepository;
import com.solomyuri.game_service.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GamesServiceImpl implements GamesService {

	private final UsersRepository usersRepository;
	private final GamesRepository gamesRepository;
	private final GameMapper gameMapper;

	@Override
	@Transactional
	public CreateGameResponse createGame(CreateGameRequest request, JwtAuthenticationToken token) {
		String username = (String) token.getToken().getClaims().get("preferred_username");

		User user = usersRepository.findByUsername(username).orElseThrow(() -> {
			log.warn("User with username {} not found", username);
			throw new ApplicationException("User not found", HttpStatus.NOT_FOUND);
		});

		if (Objects.nonNull(user.getCurrentGame()))
			gamesRepository.deleteById(user.getCurrentGame().getId());

		Game userGame = gameMapper.dtoToEntity(request.getGame());
		user.setCurrentGame(userGame);
		userGame.setOwner(user);

	    gamesRepository.saveAndFlush(userGame);

		userGame.getShips().forEach(ship -> {
			ship.setGame(userGame);
			ship.setUser(user);
			ship.getCells().forEach(cell -> {
				cell.setShip(ship);
				cell.setGame(userGame);
				cell.setUser(user);
			});
		});

		usersRepository.save(user);
		return new CreateGameResponse(userGame.getId());
	}

}
