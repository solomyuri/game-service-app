package com.solomyuri.game_service.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "cells")
public class Cell extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "x_coordinate", nullable = false)
    String x;

    @Column(name = "y_coordinate", nullable = false)
    String y;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id")
    private Ship ship;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "cell")
    private Shot shot;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Cell cell = (Cell) o;
        return Objects.equals(id, cell.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
