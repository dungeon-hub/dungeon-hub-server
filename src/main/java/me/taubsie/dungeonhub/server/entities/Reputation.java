package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity(name = "reputation")
@Table(name = "reputation", schema = "dungeon-hub")
public class Reputation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "rep_amount", nullable = true)
    private int repAmt;
}
