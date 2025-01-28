package com.solomyuri.game_service.model.entity;

import com.solomyuri.game_service.enums.ShotResult;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shots")
public class Shot extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "number")
    Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    ShotResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id", nullable = false)
    private Cell cell;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Shot shot = (Shot) o;
        return Objects.equals(id, shot.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
