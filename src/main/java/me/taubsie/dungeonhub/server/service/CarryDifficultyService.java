package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyCreationModel;
import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyModel;
import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyUpdateModel;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryDifficultyInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
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
    public Optional<CarryDifficulty> loadEntityById(long id) {
        return carryDifficultyRepository.findById(id);
    }

    public Optional<CarryDifficulty> loadEntityById(CarryTier carryTier, long id) {
        return carryDifficultyRepository.findById(id)
                .filter(carryDifficulty -> carryDifficulty.getCarryTier().equals(carryTier));
    }

    @Override
    public Optional<CarryDifficulty> loadEntityByName(String name) {
        return carryDifficultyRepository.findCarryDifficultyByIdentifier(name);
    }

    public List<CarryDifficulty> findByCarryTier(CarryTier carryTier) {
        return carryDifficultyRepository.findCarryDifficultiesByCarryTier(carryTier);
    }

    @Override
    public List<CarryDifficulty> findAllEntities() {
        return carryDifficultyRepository.findAll();
    }

    public Stream<CarryDifficulty> findAllEntities(DiscordServer discordServer) {
        return carryDifficultyRepository.findAll().stream()
                .filter(carryDifficulty -> carryDifficulty.getCarryTier().getCarryType().getDiscordServer().equals(discordServer));
    }

    @Override
    public CarryDifficulty createEntity(CarryDifficultyInitializeModel initalizationModel) {
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
    public CarryDifficulty saveEntity(CarryDifficulty entity) {
        return carryDifficultyRepository.save(entity);
    }

    @Override
    public Function<CarryDifficultyModel, CarryDifficulty> toEntity() {
        return carryDifficultyModel -> carryDifficultyRepository.findById(carryDifficultyModel.getId()).orElseThrow(() -> new EntityUnknownException(carryDifficultyModel.getId()));
    }

    @Override
    public Function<CarryDifficulty, CarryDifficultyModel> toModel() {
        return CarryDifficulty::toModel;
    }
}