package com.solomyuri.game_service.model.entity;

import com.solomyuri.game_service.enums.ShipType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ships")
public class Ship extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ShipType type;

    @Column(name = "number", nullable = false)
    private Integer number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @OneToMany(mappedBy = "ship", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @Builder.Default
    private Set<Cell> cells = new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Ship ship = (Ship) o;
		return Objects.equals(number, ship.number) &&
		        Objects.equals(type, ship.type) &&
		        Objects.equals(game, ship.game) &&
		        Objects.equals(user, ship.user);
	}

    @Override
    public int hashCode() {
        return Objects.hash(number, type, game, user);
    }
}
