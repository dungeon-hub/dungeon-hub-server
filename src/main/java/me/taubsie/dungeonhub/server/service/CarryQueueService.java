package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryQueue;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.model.CarryQueueInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
import me.taubsie.dungeonhub.server.repositories.CarryQueueRepository;
import me.taubsie.dungeonhub.server.repositories.DiscordUserRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.carry_queue.CarryQueueCreationModel;
import net.dungeonhub.model.carry_queue.CarryQueueModel;
import net.dungeonhub.model.carry_queue.CarryQueueUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class CarryQueueService implements EntityService<CarryQueue, CarryQueueModel, CarryQueueCreationModel, CarryQueueInitializeModel, CarryQueueUpdateModel> {
    private final CarryQueueRepository carryQueueRepository;
    private final DiscordUserRepository discordUserRepository;
    private final CarryDifficultyRepository carryDifficultyRepository;

    @Autowired
    public CarryQueueService(CarryQueueRepository carryQueueRepository, DiscordUserRepository discordUserRepository, CarryDifficultyRepository carryDifficultyRepository) {
        this.carryQueueRepository = carryQueueRepository;
        this.discordUserRepository = discordUserRepository;
        this.carryDifficultyRepository = carryDifficultyRepository;
    }

    public CarryQueue getCarryQueue(Long id) {
        return carryQueueRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void deleteCarryQueue(Long id) {
        carryQueueRepository.deleteById(id);
    }

    @Override
    public @NotNull Optional<CarryQueue> loadEntityById(long id) {
        return carryQueueRepository.findById(id);
    }

    @Override
    public @NotNull List<CarryQueue> findAllEntities() {
        return carryQueueRepository.findAll();
    }

    @Override
    public @NotNull CarryQueue createEntity(CarryQueueInitializeModel creationModel) {
        return saveEntity(creationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return carryQueueRepository.findById(id).map(carryQueue ->
        {
            carryQueueRepository.delete(carryQueue);
            return true;
        }).orElse(false);
    }

    @Override
    public @NotNull CarryQueue saveEntity(@NotNull CarryQueue entity) {
        return carryQueueRepository.save(entity);
    }

    @Override
    public Function<CarryQueueModel, CarryQueue> toEntity() {
        return carryQueueModel -> loadEntityById(carryQueueModel.getId()).orElseThrow(() -> new EntityUnknownException(carryQueueModel.getId()));
    }

    @Override
    public @NotNull Function<CarryQueue, CarryQueueModel> toModel() {
        return CarryQueue::toModel;
    }

    @Override
    public @NotNull CarryQueue updateEntity(@NotNull CarryQueue carryQueue, @NotNull CarryQueueUpdateModel updateModel) {
        if (updateModel.getQueueStep() != null) {
            carryQueue.setQueueStep(updateModel.getQueueStep());
        }

        if (updateModel.getCarrier() != null) {
            DiscordUser carrier = discordUserRepository.loadEntityOrCreate(updateModel.getCarrier().getId());

            carryQueue.setCarrier(carrier);
        }

        if (updateModel.getPlayer() != null) {
            DiscordUser player = discordUserRepository.loadEntityOrCreate(updateModel.getPlayer().getId());

            carryQueue.setPlayer(player);
        }

        if (updateModel.getAmount() != null) {
            carryQueue.setAmount(updateModel.getAmount());
        }

        if (updateModel.getCarryDifficulty() != null) {
            CarryDifficulty carryDifficulty = carryDifficultyRepository.findById(updateModel.getCarryDifficulty().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            carryQueue.setCarryDifficulty(carryDifficulty);
        }

        if (updateModel.getResetRelationId()) {
            carryQueue.setRelationId(null);
        }

        if (updateModel.getRelationId() != null) {
            carryQueue.setRelationId(updateModel.getRelationId());
        }

        if (updateModel.getResetAttachmentLink()) {
            carryQueue.setAttachmentLink(null);
        }

        if (updateModel.getAttachmentLink() != null) {
            carryQueue.setAttachmentLink(updateModel.getAttachmentLink());
        }

        if (updateModel.getResetTime()) {
            carryQueue.setTime(null);
        }

        if (updateModel.getTime() != null) {
            carryQueue.setTime(updateModel.getTime());
        }

        return carryQueue;
    }
}