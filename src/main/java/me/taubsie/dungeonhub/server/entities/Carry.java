package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.carry.CarryModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Getter
@Entity(name = "carry")
@Table(name = "carry", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class Carry implements EntityModelRelation<CarryModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "time")
    private Instant time;

    @Column(name = "amount", nullable = false)
    private long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "carry_difficulty")
    private CarryDifficulty carryDifficulty;

    @Column(name = "player")
    private Long player;

    @Column(name = "carrier", nullable = false)
    private long carrier;

    @Nullable
    @Setter
    @Column(name = "approver")
    private Long approver;

    @Nullable
    @Setter
    @Column(name = "attachment_link")
    private String attachmentLink;

    public Carry(long carrier, Long player, long amount, CarryDifficulty carryDifficulty,
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
    public Carry fromModel(CarryModel model) {
        return new Carry(model.id(), model.time(), model.amount(),
                carryDifficulty.fromModel(model.carryDifficulty()), model.player(), model.carrier(),
                model.approver(), model.attachmentLink());
    }

    @Override
    public CarryModel toModel() {
        return new CarryModel(id, time, amount, carryDifficulty.toModel(), player, carrier, approver, attachmentLink);
    }

    public long calculateScore() {
        return getScoreMultiplier() * amount;
    }

    private long getScoreMultiplier() {
        return carryDifficulty.getScore();
    }
}