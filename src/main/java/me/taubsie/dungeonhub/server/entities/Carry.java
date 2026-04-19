package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.carry.CarryModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Getter
@Entity(name = "carry")
@Table(name = "carry", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class Carry implements net.dungeonhub.structure.entity.Entity<CarryModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "time")
    private Instant time;

    @Column(name = "amount", nullable = false)
    private int amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "carry_difficulty")
    private CarryDifficulty carryDifficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player", nullable = false)
    private DiscordUser player;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "carrier", nullable = false)
    private DiscordUser carrier;

    @Nullable
    @Setter
    @Column(name = "approver")
    private Long approver;

    @Nullable
    @Setter
    @Column(name = "attachment_link")
    private String attachmentLink;

    public Carry(DiscordUser carrier, DiscordUser player, int amount, CarryDifficulty carryDifficulty,
                 @Nullable String attachmentLink, Instant time) {
        this.carrier = carrier;
        this.player = player;
        this.amount = amount;
        this.carryDifficulty = carryDifficulty;
        this.attachmentLink = attachmentLink;
        this.time = time;
    }

    public CarryTier getCarryTier() {
        return getCarryDifficulty().getCarryTier();
    }

    public CarryType getCarryType() {
        return getCarryTier().getCarryType();
    }

    @Override
    public @NotNull CarryModel toModel() {
        return new CarryModel(
                id,
                amount,
                carryDifficulty.toModel(),
                player.toModel(),
                carrier.toModel(),
                approver,
                attachmentLink,
                time
        );
    }

    public long calculateScore() {
        return getScoreMultiplier() * amount;
    }

    private long getScoreMultiplier() {
        return carryDifficulty.getScore();
    }

    public long calculateTotalPrice() {
        return carryDifficulty.calculateTotalPrice(amount);
    }
}