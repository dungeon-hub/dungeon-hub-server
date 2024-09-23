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
import org.jetbrains.annotations.NotNull;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "player")
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

    public Carry(DiscordUser carrier, DiscordUser player, long amount, CarryDifficulty carryDifficulty,
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
    public @NotNull Carry fromModel(@NotNull CarryModel model) {
        return new Carry(model.id(), model.time(), model.amount(), carryDifficulty.fromModel(model.carryDifficulty()),
                player.fromModel(model.player()), carrier.fromModel(model.carrier()), model.approver(),
                model.attachmentLink());
    }

    @Override
    public @NotNull CarryModel toModel() {
        return new CarryModel(id, time, amount, carryDifficulty.toModel(), player.toModel(), carrier.toModel(),
                approver, attachmentLink);
    }

    public long calculateScore() {
        return getScoreMultiplier() * amount;
    }

    private long getScoreMultiplier() {
        return carryDifficulty.getScore();
    }

    //TODO merge this method and the one from ApplicationService into common?
    public long calculatePrice() {
        Integer bulkPrice = carryDifficulty.getBulkPrice();
        Integer bulkAmount = carryDifficulty.getBulkAmount();

        if (bulkPrice != null && bulkAmount != null && bulkAmount <= amount) {
            return bulkPrice;
        }

        return carryDifficulty.getPrice();
    }
}