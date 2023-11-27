package me.taubsie.dungeonhub.server.service;

import com.google.errorprone.annotations.DoNotCall;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.common.model.carry_type.CarryTypeCreationModel;
import me.taubsie.dungeonhub.common.model.carry_type.CarryTypeModel;
import me.taubsie.dungeonhub.common.model.carry_type.CarryTypeUpdateModel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryTypeInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    public Optional<CarryType> loadEntityById(long id) {
        return carryTypeRepository.findById(id);
    }

    public Optional<CarryType> loadEntityById(DiscordServer discordServer, long id) {
        return carryTypeRepository.findById(id)
                .filter(carryType -> carryType.getDiscordServer().getId() == discordServer.getId());
    }

    public List<CarryType> loadEntitiesByDiscordServer(DiscordServer discordServer) {
        return carryTypeRepository.findCarryTypesByDiscordServer(discordServer);
    }

    public Map<Long, CarryType> getCarryTypeMap() {
        return carryTypeRepository.getCarryTypeMap();
    }

    @Override
    @DoNotCall
    public Optional<CarryType> loadEntityByName(String name) {
        return carryTypeRepository.findByIdentifier(name);
    }

    @Override
    public List<CarryType> findAllEntities() {
        return carryTypeRepository.findAll();
    }

    @Override
    public CarryType createEntity(CarryTypeInitializeModel initalizationModel) {
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
    public Function<CarryType, CarryTypeModel> toModel() {
        return CarryType::toModel;
    }

    @Override
    public CarryType saveEntity(CarryType carryType) {
        return carryTypeRepository.save(carryType);
    }
}