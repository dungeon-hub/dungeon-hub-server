package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryTierInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryTierRepository;
import net.dungeonhub.expections.EntityUnknownException;
import net.dungeonhub.model.carry_tier.CarryTierCreationModel;
import net.dungeonhub.model.carry_tier.CarryTierModel;
import net.dungeonhub.model.carry_tier.CarryTierUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class CarryTierService implements EntityService<CarryTier, CarryTierModel, CarryTierCreationModel, CarryTierInitializeModel, CarryTierUpdateModel> {
    private final CarryTierRepository carryTierRepository;

    @Autowired
    public CarryTierService(CarryTierRepository carryTierRepository) {
        this.carryTierRepository = carryTierRepository;
    }

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
        return carryTierRepository.save(initalizationModel.toEntity());
    }

    public void delete(CarryTier carryTier) {
        carryTierRepository.delete(carryTier);
    }

    @Override
    public boolean delete(long id) {
        return carryTierRepository.findById(id).map(entity ->
        {
            carryTierRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public @NotNull CarryTier saveEntity(@NotNull CarryTier entity) {
        return carryTierRepository.save(entity);
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

        if (carryTierUpdateModel.getResetPriceChannel()) {
            carryTier.setPriceChannel(null);
        }

        if (carryTierUpdateModel.getPriceChannel() != null) {
            carryTier.setPriceChannel(carryTierUpdateModel.getPriceChannel());
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