package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryDifficultyInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyHistoryRepository;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class CarryDifficultyService implements EntityService<CarryDifficulty, CarryDifficultyModel,
        CarryDifficultyCreationModel, CarryDifficultyInitializeModel, CarryDifficultyUpdateModel> {
    /**
     * Start of the first history period of every difficulty. It is the smallest value a MariaDB {@code TIMESTAMP}
     * column can hold, so the first period covers every carry that happened before the first pricing change.
     */
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
     * Loads the pricing history of all given difficulties in a single query.
     */
    public List<CarryDifficultyHistory> loadPriceHistory(Collection<Long> carryDifficultyIds) {
        if (carryDifficultyIds.isEmpty()) {
            return List.of();
        }

        return carryDifficultyHistoryRepository.findAllByCarryDifficulty_IdIn(carryDifficultyIds);
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

    /**
     * Applies the update and saves the difficulty. Runs in one transaction, so the history entry written by
     * {@link #updateEntity} is only persisted together with the changed difficulty.
     */
    @Override
    @Transactional
    public @NotNull CarryDifficulty update(@NotNull CarryDifficulty carryDifficulty, @NotNull CarryDifficultyUpdateModel carryDifficultyUpdateModel) {
        return saveEntity(updateEntity(carryDifficulty, carryDifficultyUpdateModel));
    }

    @Override
    public @NotNull CarryDifficulty updateEntity(@NotNull CarryDifficulty carryDifficulty, @NotNull CarryDifficultyUpdateModel carryDifficultyUpdateModel) {
        Pricing previousPricing = Pricing.of(carryDifficulty);

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

        if (!previousPricing.equals(Pricing.of(carryDifficulty))) {
            recordReplacedPricing(carryDifficulty, previousPricing);
        }

        return carryDifficulty;
    }

    /**
     * Stores the replaced pricing as a closed period. The period starts where the previous one ended, so the history
     * of a difficulty has no gaps, and ends now, which is when the new pricing becomes valid.
     */
    private void recordReplacedPricing(CarryDifficulty carryDifficulty, Pricing replacedPricing) {
        Instant validFrom = carryDifficultyHistoryRepository
                .findFirstByCarryDifficultyOrderByDateToDesc(carryDifficulty)
                .map(CarryDifficultyHistory::getDateTo)
                .orElse(HISTORY_START);

        carryDifficultyHistoryRepository.save(new CarryDifficultyHistory(carryDifficulty, replacedPricing.price(),
                replacedPricing.bulkPrice(), replacedPricing.bulkAmount(), replacedPricing.score(), validFrom,
                Instant.now()));
    }

    /**
     * The values of a difficulty that are tracked in its history.
     */
    private record Pricing(int price, Integer bulkPrice, Integer bulkAmount, int score) {
        static Pricing of(CarryDifficulty carryDifficulty) {
            return new Pricing(carryDifficulty.getPrice(), carryDifficulty.getBulkPrice(),
                    carryDifficulty.getBulkAmount(), carryDifficulty.getScore());
        }
    }
}
