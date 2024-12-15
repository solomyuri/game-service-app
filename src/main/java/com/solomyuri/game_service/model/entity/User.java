package com.solomyuri.game_service.model.entity;

import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

	@Id
	@Column(name = "username")
	private String username;

	@Column(name = "game_count")
	private Integer gameCount;

	@Column(name = "win_count")
	private Integer winCount;

	@Column(name = "lose_count")
	private Integer loseCount;

	@OneToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "current_game_id", referencedColumnName = "id")
	private Game currentGame;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		User user = (User) o;
		return Objects.equals(username, user.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}
}
