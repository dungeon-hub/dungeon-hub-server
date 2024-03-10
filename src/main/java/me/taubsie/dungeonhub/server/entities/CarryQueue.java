package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.enums.QueueStep;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@Getter
@Setter
@Entity(name = "carry_queue")
@Table(name = "carry_queue", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class CarryQueue implements EntityModelRelation<CarryQueueModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "queue_step", nullable = false)
    @Enumerated
    private QueueStep queueStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "carrier", nullable = false)
    private DiscordUser carrier;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "player")
    private DiscordUser player;

    @Column(name = "amount", nullable = false)
    private long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JoinColumn(name = "carry_difficulty")
    private CarryDifficulty carryDifficulty;

    //This is the id of the discord entity that it relates to
    //If the queue step is CONFIRMATION or TRANSCRIPT, this relates to the ticket channel
    //If the queue step is APPROVING, this relates to the approving message
    @Column(name = "relation_id")
    private Long relationId;

    @Column(name = "attachment_link")
    private String attachmentLink;

    @Column(name = "time")
    private Instant time;

    @SuppressWarnings("java:S107")
    public CarryQueue(QueueStep queueStep, DiscordUser carrier, DiscordUser player, long amount,
                      CarryDifficulty carryDifficulty, Long relationId, String attachmentLink, Instant time) {
        this.queueStep = queueStep;
        this.carrier = carrier;
        this.player = player;
        this.amount = amount;
        this.carryDifficulty = carryDifficulty;
        this.relationId = relationId;
        this.attachmentLink = attachmentLink;
        this.time = time;
    }

    public CarryType getCarryType() {
        return getCarryTier().getCarryType();
    }

    public CarryTier getCarryTier() {
        return getCarryDifficulty().getCarryTier();
    }

    @Override
    public @NotNull CarryQueue fromModel(@NotNull CarryQueueModel model) {
        return new CarryQueue(model.getId(), model.getQueueStep(), carrier.fromModel(model.getCarrier()),
                player.fromModel(model.getPlayer()), model.getAmount(),
                carryDifficulty.fromModel(model.getCarryDifficulty()), model.getRelationId(),
                model.getAttachmentLink(), model.getTime());
    }

    @Override
    public @NotNull CarryQueueModel toModel() {
        return new CarryQueueModel(id, queueStep, carrier.toModel(), player.toModel(), amount,
                carryDifficulty.toModel(), relationId,
                attachmentLink, time);
    }

    public Carry toCarry() {
        return new Carry(carrier, player, amount, carryDifficulty, attachmentLink, time);
    }
}