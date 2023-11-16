package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.DungeonHubService;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.enums.QueueStep;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueCreationModel;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryQueue;
import me.taubsie.dungeonhub.server.service.CarryDifficultyService;

import java.time.Instant;

@AllArgsConstructor
public class CarryQueueInitializeModel implements InitializeModel<CarryQueue, CarryQueueCreationModel> {
    private QueueStep queueStep;
    private Long carrier;
    private Long player;
    private Long amount;
    private CarryDifficulty carryDifficulty;
    private Long relationId;
    private String attachmentLink;
    private Instant time;

    public CarryQueueInitializeModel(CarryDifficulty carryDifficulty) {
        this.carryDifficulty = carryDifficulty;
    }

    @Override
    public CarryQueue toEntity() {
        return new CarryQueue(queueStep, carrier, player, amount, carryDifficulty, relationId, attachmentLink,
                time);
    }

    @Override
    public CarryQueueInitializeModel fromCreationModel(CarryQueueCreationModel creationModel) {
        return new CarryQueueInitializeModel(creationModel.getQueueStep(), creationModel.getCarrier(),
                creationModel.getPlayer(), creationModel.getAmount(),
                carryDifficulty, creationModel.getRelationId(),
                creationModel.getAttachmentLink(), creationModel.getTime());
    }

    public String toJson() {
        return DungeonHubService.getInstance()
                .getGson()
                .toJson(this);
    }
}