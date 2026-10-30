package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.changes.ChangeAudience;
import me.taubsie.dungeonhub.server.changes.ChangePublisher;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryTierInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryTierRepository;
import net.dungeonhub.enums.ChangeAction;
import net.dungeonhub.enums.ChangeEventType;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.carry_tier.CarryTierCreationModel;
import net.dungeonhub.model.carry_tier.CarryTierModel;
import net.dungeonhub.model.carry_tier.CarryTierUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class CarryTierService implements EntityService<CarryTier, CarryTierModel, CarryTierCreationModel, CarryTierInitializeModel, CarryTierUpdateModel> {
    private final CarryTierRepository carryTierRepository;
    private final ChangePublisher changePublisher;

    @Override
    public @NotNull Optional<CarryTier> loadEntityById(long id) {
        return carryTierRepository.findById(id);
    }

    public Optional<CarryTier> loadEntityById(CarryType carryType, long id) {
        return carryTierRepository.findById(id)
                .filter(carryTier -> carryTier.getCarryType().equals(carryType));
    }

    public List<CarryTier> loadEntitiesByCarryType(CarryType carryType) {
        return carryTierRepository.findCarryTiersByCarryType(carryType);
    }

    @Override
    public @NotNull List<CarryTier> findAllEntities() {
        return carryTierRepository.findAll();
    }

    public Stream<CarryTier> findAllEntities(DiscordServer discordServer) {
        return findAllEntities().stream().filter(carryTier -> carryTier.getCarryType().getDiscordServer().equals(discordServer));
    }

    @Override
    public @NotNull CarryTier createEntity(CarryTierInitializeModel initalizationModel) {
        return saveEntity(initalizationModel.toEntity());
    }

    public void delete(CarryTier carryTier) {
        CarryTierModel model = carryTier.toModel();
        carryTierRepository.delete(carryTier);
        publishChange(ChangeAction.Deleted, model);
    }

    @Override
    public boolean delete(long id) {
        return carryTierRepository.findById(id).map(entity ->
        {
            delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public @NotNull CarryTier saveEntity(@NotNull CarryTier entity) {
        boolean created = entity.getId() == 0;
        CarryTier carryTier = carryTierRepository.save(entity);
        publishChange(created ? ChangeAction.Created : ChangeAction.Updated, carryTier.toModel());
        return carryTier;
    }

    private void publishChange(ChangeAction action, CarryTierModel carryTier) {
        long serverId = carryTier.getCarryType().getServer().getId();
        changePublisher.publish(ChangeEventType.CarryTier, action, serverId, carryTier, ChangeAudience.server(serverId));
    }

    @Override
    public Function<CarryTierModel, CarryTier> toEntity() {
        return carryTierModel -> carryTierRepository.findById(carryTierModel.getId())
                .orElseThrow(() -> new EntityUnknownException(carryTierModel.getId()));
    }

    @Override
    public @NotNull Function<CarryTier, CarryTierModel> toModel() {
        return CarryTier::toModel;
    }

    public Optional<CarryTier> findByCategory(DiscordServer discordServer, long categoryId) {
        return carryTierRepository.findFirstByCarryType_DiscordServerAndCategory(discordServer, categoryId);
    }

    @Override
    public @NotNull CarryTier updateEntity(@NotNull CarryTier carryTier, @NotNull CarryTierUpdateModel carryTierUpdateModel) {
        if (carryTierUpdateModel.getDisplayName() != null) {
            carryTier.setDisplayName(carryTierUpdateModel.getDisplayName());
        }

        if (carryTierUpdateModel.getResetCategory()) {
            carryTier.setCategory(null);
        }

        if (carryTierUpdateModel.getCategory() != null) {
            carryTier.setCategory(carryTierUpdateModel.getCategory());
        }

        if (carryTierUpdateModel.getResetDescriptiveName()) {
            carryTier.setDescriptiveName(null);
        }

        if (carryTierUpdateModel.getDescriptiveName() != null) {
            carryTier.setDescriptiveName(carryTierUpdateModel.getDescriptiveName());
        }

        if (carryTierUpdateModel.getResetThumbnailUrl()) {
            carryTier.setThumbnailUrl(null);
        }

        if (carryTierUpdateModel.getThumbnailUrl() != null) {
            carryTier.setThumbnailUrl(carryTierUpdateModel.getThumbnailUrl());
        }

        if (carryTierUpdateModel.getResetPriceTitle()) {
            carryTier.setPriceTitle(null);
        }

        if (carryTierUpdateModel.getPriceTitle() != null) {
            carryTier.setPriceTitle(carryTierUpdateModel.getPriceTitle());
        }

        if (carryTierUpdateModel.getResetPriceDescription()) {
            carryTier.setPriceDescription(null);
        }

        if (carryTierUpdateModel.getPriceDescription() != null) {
            carryTier.setPriceDescription(carryTierUpdateModel.getPriceDescription());
        }

        return carryTier;
    }
}