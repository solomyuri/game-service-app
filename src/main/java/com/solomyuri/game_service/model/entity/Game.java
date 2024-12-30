package com.solomyuri.game_service.model.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "games")
public class Game extends BaseEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "current_shooter")
	private User currentShooter;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner", nullable = false)
	private User owner;

	@OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REMOVE})
	private Set<Cell> cells = new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Game game = (Game) o;
		return Objects.equals(id, game.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
