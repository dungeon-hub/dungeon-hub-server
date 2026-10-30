package me.taubsie.dungeonhub.server.service;

import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.server.changes.ChangeAudience;
import me.taubsie.dungeonhub.server.changes.ChangePublisher;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryTypeInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryTypeRepository;
import net.dungeonhub.enums.ChangeAction;
import net.dungeonhub.enums.ChangeEventType;
import net.dungeonhub.exceptions.EntityUnknownException;
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
    private ChangePublisher changePublisher;

    @Autowired
    public CarryTypeService(CarryTypeRepository carryTypeRepository, ChangePublisher changePublisher) {
        this.carryTypeRepository = carryTypeRepository;
        this.changePublisher = changePublisher;
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
            delete(entity);
            return true;
        }).orElse(false);
    }

    public void delete(CarryType carryType) {
        CarryTypeModel model = carryType.toModel();
        carryTypeRepository.delete(carryType);
        publishChange(ChangeAction.Deleted, model);
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
        boolean created = carryType.getId() == 0;
        CarryType savedCarryType = carryTypeRepository.save(carryType);
        publishChange(created ? ChangeAction.Created : ChangeAction.Updated, savedCarryType.toModel());
        return savedCarryType;
    }

    private void publishChange(ChangeAction action, CarryTypeModel carryType) {
        long serverId = carryType.getServer().getId();
        changePublisher.publish(ChangeEventType.CarryType, action, serverId, carryType, ChangeAudience.server(serverId));
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

        if(carryTypeUpdateModel.getResetEventActive()) {
            carryType.setEventActive(null);
        }

        if (carryTypeUpdateModel.getEventActive() != null) {
            carryType.setEventActive(carryTypeUpdateModel.getEventActive());
        }

        return carryType;
    }
}