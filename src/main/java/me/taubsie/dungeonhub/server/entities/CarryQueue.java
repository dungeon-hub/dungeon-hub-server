package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.DungeonHubService;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.enums.QueueStep;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueModel;

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

    //TODO make this into carrier table
    @Column(name = "carrier", nullable = false)
    private long carrier;

    //TODO make this into user table?
    @Column(name = "player")
    private Long player;

    @Column(name = "amount", nullable = false)
    private long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
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
    public CarryQueue(QueueStep queueStep, long carrier, Long player, long amount, CarryDifficulty carryDifficulty,
                      Long relationId, String attachmentLink, Instant time) {
        this.queueStep = queueStep;
        this.carrier = carrier;
        this.player = player;
        this.amount = amount;
        this.carryDifficulty = carryDifficulty;
        this.relationId = relationId;
        this.attachmentLink = attachmentLink;
        this.time = time;
    }

    public static CarryQueue fromJson(String json) {
        return DungeonHubService.getInstance().getGson().fromJson(json, CarryQueue.class);
    }

    public CarryType getCarryType() {
        return getCarryTier().getCarryType();
    }

    public CarryTier getCarryTier() {
        return getCarryDifficulty().getCarryTier();
    }

    @Override
    public CarryQueue fromModel(CarryQueueModel model) {
        return new CarryQueue(model.getId(), model.getQueueStep(), model.getCarrier(), model.getPlayer(),
                model.getAmount(), carryDifficulty.fromModel(model.getCarryDifficulty()), model.getRelationId(),
                model.getAttachmentLink(), model.getTime());
    }

    @Override
    public CarryQueueModel toModel() {
        return new CarryQueueModel(id, queueStep, carrier, player, amount, carryDifficulty.toModel(), relationId,
                attachmentLink, time);
    }

    public Carry toCarry() {
        return new Carry(carrier, player, amount, carryDifficulty, attachmentLink, time);
    }
}