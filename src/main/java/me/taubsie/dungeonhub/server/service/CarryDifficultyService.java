package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryDifficultyInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
import net.dungeonhub.expections.EntityUnknownException;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyCreationModel;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyModel;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class CarryDifficultyService implements EntityService<CarryDifficulty, CarryDifficultyModel,
        CarryDifficultyCreationModel, CarryDifficultyInitializeModel, CarryDifficultyUpdateModel> {
    private final CarryDifficultyRepository carryDifficultyRepository;

    @Autowired
    public CarryDifficultyService(CarryDifficultyRepository carryDifficultyRepository) {
        this.carryDifficultyRepository = carryDifficultyRepository;
    }

    @Override
    public @NotNull Optional<CarryDifficulty> loadEntityById(long id) {
        return carryDifficultyRepository.findById(id);
    }

    public Optional<CarryDifficulty> loadEntityById(CarryTier carryTier, long id) {
        return carryDifficultyRepository.findById(id)
                .filter(carryDifficulty -> carryDifficulty.getCarryTier().equals(carryTier));
    }

    public List<CarryDifficulty> findByCarryTier(CarryTier carryTier) {
        return carryDifficultyRepository.findCarryDifficultiesByCarryTier(carryTier);
    }

    @Override
    public @NotNull List<CarryDifficulty> findAllEntities() {
        return carryDifficultyRepository.findAll();
    }

    public Stream<CarryDifficulty> findAllEntities(DiscordServer discordServer) {
        return carryDifficultyRepository.findAll().stream()
                .filter(carryDifficulty -> carryDifficulty.getCarryTier().getCarryType().getDiscordServer().equals(discordServer));
    }

    @Override
    public @NotNull CarryDifficulty createEntity(CarryDifficultyInitializeModel initalizationModel) {
        return carryDifficultyRepository.save(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return carryDifficultyRepository.findById(id).map(entity ->
        {
            carryDifficultyRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public @NotNull CarryDifficulty saveEntity(@NotNull CarryDifficulty entity) {
        return carryDifficultyRepository.save(entity);
    }

    @Override
    public Function<CarryDifficultyModel, CarryDifficulty> toEntity() {
        return carryDifficultyModel -> carryDifficultyRepository.findById(carryDifficultyModel.getId()).orElseThrow(() -> new EntityUnknownException(carryDifficultyModel.getId()));
    }

    @Override
    public @NotNull Function<CarryDifficulty, CarryDifficultyModel> toModel() {
        return CarryDifficulty::toModel;
    }

    @Override
    public @NotNull CarryDifficulty updateEntity(@NotNull CarryDifficulty carryDifficulty, @NotNull CarryDifficultyUpdateModel carryDifficultyUpdateModel) {
        if(carryDifficultyUpdateModel.getDisplayName() != null) {
            carryDifficulty.setDisplayName(carryDifficultyUpdateModel.getDisplayName());
        }

        if(carryDifficultyUpdateModel.getResetThumbnailUrl()) {
            carryDifficulty.setThumbnailUrl(null);
        }

        if(carryDifficultyUpdateModel.getThumbnailUrl() != null) {
            carryDifficulty.setThumbnailUrl(carryDifficultyUpdateModel.getThumbnailUrl());
        }

        if(carryDifficultyUpdateModel.getResetBulkPrice()) {
            carryDifficulty.setBulkPrice(null);
        }

        if(carryDifficultyUpdateModel.getBulkPrice() != null) {
            carryDifficulty.setBulkPrice(carryDifficultyUpdateModel.getBulkPrice());
        }

        if(carryDifficultyUpdateModel.getResetBulkAmount()) {
            carryDifficulty.setBulkAmount(null);
        }

        if(carryDifficultyUpdateModel.getBulkAmount() != null) {
            carryDifficulty.setBulkAmount(carryDifficultyUpdateModel.getBulkAmount());
        }

        if(carryDifficultyUpdateModel.getResetPriceName()) {
            carryDifficulty.setPriceName(null);
        }

        if(carryDifficultyUpdateModel.getPriceName() != null) {
            carryDifficulty.setPriceName(carryDifficultyUpdateModel.getPriceName());
        }

        if(carryDifficultyUpdateModel.getPrice() != null) {
            carryDifficulty.setPrice(carryDifficultyUpdateModel.getPrice());
        }

        if(carryDifficultyUpdateModel.getScore() != null) {
            carryDifficulty.setScore(carryDifficultyUpdateModel.getScore());
        }

        return carryDifficulty;
    }
}