package com.solomyuri.game_service.model.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "users")
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id")
	private UUID id;

	@Column(name = "username")
	private String username;

	@Column(name = "game_count")
	@Builder.Default
	private Integer gameCount = 0;

	@Column(name = "win_count")
	@Builder.Default
	private Integer winCount = 0;

	@Column(name = "lose_count")
	@Builder.Default
	private Integer loseCount = 0;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "email_verified")
	@Builder.Default
	private Boolean emailVerified = false;
	
	@Column(name = "is_admin")
	@Builder.Default
	private Boolean isAdmin = false;
	
	@Column(name = "is_blocked")
	@Builder.Default
	private Boolean isBlocked = false;

	@OneToOne(cascade = {CascadeType.MERGE}, mappedBy = "owner", fetch = FetchType.LAZY)
	private Game currentGame;

	@OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<Cell> cells = new HashSet<>();

	@OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<Ship> ships = new HashSet<>();

	@OneToMany(cascade = {CascadeType.MERGE}, mappedBy = "user", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<Shot> shots = new HashSet<>();

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
