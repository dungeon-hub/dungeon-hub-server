package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryQueue;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import net.dungeonhub.enums.QueueStep;
import net.dungeonhub.model.carry_queue.CarryQueueCreationModel;
import net.dungeonhub.model.carry_queue.CarryQueueModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@AllArgsConstructor
public class CarryQueueInitializeModel implements InitializeModel<CarryQueue, CarryQueueModel, CarryQueueCreationModel> {
    private QueueStep queueStep;
    private DiscordUser carrier;
    private DiscordUser player;

    private int amount;
    private CarryDifficulty carryDifficulty;
    private Long relationId;
    private String attachmentLink;
    private Instant time;

    public CarryQueueInitializeModel(CarryDifficulty carryDifficulty, DiscordUser player, DiscordUser carrier) {
        this.carryDifficulty = carryDifficulty;
        this.player = player;
        this.carrier = carrier;
    }

    @Override
    public @NotNull CarryQueue toEntity() {
        return new CarryQueue(queueStep, carrier, player, amount, carryDifficulty, relationId, attachmentLink,
                time);
    }

    @Override
    public @NotNull CarryQueueInitializeModel fromCreationModel(CarryQueueCreationModel creationModel) {
        return new CarryQueueInitializeModel(creationModel.getQueueStep(), carrier, player, creationModel.getAmount(),
                carryDifficulty, creationModel.getRelationId(), creationModel.getAttachmentLink(),
                creationModel.getTime());
    }
}