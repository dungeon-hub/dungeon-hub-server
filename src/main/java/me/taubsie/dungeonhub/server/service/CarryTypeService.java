package me.taubsie.dungeonhub.server.service;

import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryTypeInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryTypeRepository;
import net.dungeonhub.expections.EntityUnknownException;
import net.dungeonhub.model.carry_type.CarryTypeCreationModel;
import net.dungeonhub.model.carry_type.CarryTypeModel;
import net.dungeonhub.model.carry_type.CarryTypeUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@NoArgsConstructor
public class CarryTypeService implements EntityService<CarryType, CarryTypeModel, CarryTypeCreationModel, CarryTypeInitializeModel, CarryTypeUpdateModel> {
    private CarryTypeRepository carryTypeRepository;

    @Autowired
    public CarryTypeService(CarryTypeRepository carryTypeRepository) {
        this.carryTypeRepository = carryTypeRepository;
    }

    @Override
    public @NotNull Optional<CarryType> loadEntityById(long id) {
        return carryTypeRepository.findById(id);
    }

    public Optional<CarryType> loadEntityById(DiscordServer discordServer, long id) {
        return carryTypeRepository.findById(id)
                .filter(carryType -> carryType.getDiscordServer().getId() == discordServer.getId());
    }

    public List<CarryType> loadEntitiesByDiscordServer(DiscordServer discordServer) {
        return carryTypeRepository.findCarryTypesByDiscordServer(discordServer);
    }

    @Override
    public @NotNull List<CarryType> findAllEntities() {
        return carryTypeRepository.findAll();
    }

    @Override
    public @NotNull CarryType createEntity(CarryTypeInitializeModel initalizationModel) {
        return saveEntity(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return carryTypeRepository.findById(id).map(entity ->
        {
            carryTypeRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    public void delete(CarryType carryType) {
        carryTypeRepository.delete(carryType);
    }

    @Override
    public Function<CarryTypeModel, CarryType> toEntity() {
        return carryTypeModel -> carryTypeRepository.findById(carryTypeModel.getId()).orElseThrow(() -> new EntityUnknownException(carryTypeModel.getId()));
    }

    @Override
    public @NotNull Function<CarryType, CarryTypeModel> toModel() {
        return CarryType::toModel;
    }

    @Override
    public @NotNull CarryType saveEntity(@NotNull CarryType carryType) {
        return carryTypeRepository.save(carryType);
    }

    @Override
    public @NotNull CarryType updateEntity(@NotNull CarryType carryType, @NotNull CarryTypeUpdateModel carryTypeUpdateModel) {
        if (carryTypeUpdateModel.getDisplayName() != null) {
            carryType.setDisplayName(carryTypeUpdateModel.getDisplayName());
        }

        if(carryTypeUpdateModel.getResetLogChannel()) {
            carryType.setLogChannel(null);
        }

        if (carryTypeUpdateModel.getLogChannel() != null) {
            carryType.setLogChannel(carryTypeUpdateModel.getLogChannel());
        }

        if(carryTypeUpdateModel.getResetLeaderboardChannel()) {
            carryType.setLeaderboardChannel(null);
        }

        if (carryTypeUpdateModel.getLeaderboardChannel() != null) {
            carryType.setLeaderboardChannel(carryTypeUpdateModel.getLeaderboardChannel());
        }

        if(carryTypeUpdateModel.getResetEventActive()) {
            carryType.setEventActive(null);
        }

        if (carryTypeUpdateModel.getEventActive() != null) {
            carryType.setEventActive(carryTypeUpdateModel.getEventActive());
        }

        return carryType;
    }
}