package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryDifficultyInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyHistoryRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyCreationModel;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyModel;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class CarryDifficultyService implements EntityService<CarryDifficulty, CarryDifficultyModel,
        CarryDifficultyCreationModel, CarryDifficultyInitializeModel, CarryDifficultyUpdateModel> {
    private static final Instant HISTORY_START = Instant.parse("1970-01-01T00:00:01Z");
    private final CarryDifficultyRepository carryDifficultyRepository;
    private final CarryDifficultyHistoryRepository carryDifficultyHistoryRepository;

    @Autowired
    public CarryDifficultyService(CarryDifficultyRepository carryDifficultyRepository,
                                  CarryDifficultyHistoryRepository carryDifficultyHistoryRepository) {
        this.carryDifficultyRepository = carryDifficultyRepository;
        this.carryDifficultyHistoryRepository = carryDifficultyHistoryRepository;
    }

    @Override
    public @NotNull Optional<CarryDifficulty> loadEntityById(long id) {
        return carryDifficultyRepository.findById(id);
    }

    public Optional<CarryDifficulty> loadEntityById(CarryTier carryTier, long id) {
        return carryDifficultyRepository.findById(id)
                .filter(carryDifficulty -> carryDifficulty.getCarryTier().getId() == carryTier.getId());
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

    /**
     * Loads the closed price and score periods for all supplied difficulties in one query.
     *
     * <p>This method is intended to support request-local calculations such as
     * {@link CarryService#historicalPriceCalculator(List)}. Prefer one bulk call over loading history separately for
     * every carry. An empty collection is handled without accessing the repository.</p>
     *
     * @param carryDifficulties distinct or repeated difficulties whose history is required
     * @return history ordered by descending start time, or an immutable empty list when no difficulties were supplied
     */
    public List<CarryDifficultyHistory> loadPriceHistory(Collection<CarryDifficulty> carryDifficulties) {
        if (carryDifficulties.isEmpty()) {
            return List.of();
        }
        return carryDifficultyHistoryRepository
                .findAllByCarryDifficultyInOrderByDateFromDesc(carryDifficulties);
    }

    @Override
    public @NotNull CarryDifficulty createEntity(CarryDifficultyInitializeModel initalizationModel) {
        return carryDifficultyRepository.save(initalizationModel.toEntity());
    }

    public void delete(CarryDifficulty carryDifficulty) {
        carryDifficultyRepository.delete(carryDifficulty);
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
    @Transactional
    public @NotNull CarryDifficulty updateEntity(@NotNull CarryDifficulty carryDifficulty, @NotNull CarryDifficultyUpdateModel carryDifficultyUpdateModel) {
        int oldPrice = carryDifficulty.getPrice();
        Integer oldBulkPrice = carryDifficulty.getBulkPrice();
        Integer oldBulkAmount = carryDifficulty.getBulkAmount();
        int oldScore = carryDifficulty.getScore();
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

        if(carryDifficultyUpdateModel.getResetIngameCarryType()) {
            carryDifficulty.setIngameCarryType(null);
        }

        if(carryDifficultyUpdateModel.getIngameCarryType() != null) {
            carryDifficulty.setIngameCarryType(carryDifficultyUpdateModel.getIngameCarryType());
        }

        if (oldPrice != carryDifficulty.getPrice()
                || !Objects.equals(oldBulkPrice, carryDifficulty.getBulkPrice())
                || !Objects.equals(oldBulkAmount, carryDifficulty.getBulkAmount())
                || oldScore != carryDifficulty.getScore()) {
            Instant changedAt = Instant.now();
            Instant dateFrom = carryDifficultyHistoryRepository
                    .findFirstByCarryDifficultyOrderByDateToDesc(carryDifficulty)
                    .map(CarryDifficultyHistory::getDateTo)
                    .orElse(HISTORY_START);
            carryDifficultyHistoryRepository.save(new CarryDifficultyHistory(carryDifficulty, oldPrice,
                    oldBulkPrice, oldBulkAmount, oldScore, dateFrom, changedAt));
        }

        return carryDifficulty;
    }

}
