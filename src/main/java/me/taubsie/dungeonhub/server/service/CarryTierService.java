package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierCreationModel;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierModel;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierUpdateModel;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.Server;
import me.taubsie.dungeonhub.server.model.CarryTierInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryTierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class CarryTierService implements EntityService<CarryTier, CarryTierModel, CarryTierCreationModel, CarryTierInitializeModel, CarryTierUpdateModel> {
    private final CarryTierRepository carryTierRepository;

    @Autowired
    public CarryTierService(CarryTierRepository carryTierRepository) {
        this.carryTierRepository = carryTierRepository;
    }

    @Override
    public Optional<CarryTier> loadEntityById(long id) {
        return carryTierRepository.findById(id);
    }

    public Optional<CarryTier> loadEntityById(CarryType carryType, long id) {
        return carryTierRepository.findById(id)
                .filter(carryTier -> carryTier.getCarryType().equals(carryType));
    }

    @Override
    public Optional<CarryTier> loadEntityByName(String name) {
        return carryTierRepository.findCarryTierByIdentifier(name);
    }

    public List<CarryTier> loadEntitiesByCarryType(CarryType carryType) {
        return carryTierRepository.findCarryTiersByCarryType(carryType);
    }

    @Override
    public List<CarryTier> findAllEntities() {
        return carryTierRepository.findAll();
    }

    @Override
    public CarryTier createEntity(CarryTierInitializeModel initalizationModel) {
        return carryTierRepository.save(initalizationModel.toEntity());
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
    public CarryTier saveEntity(CarryTier entity) {
        return carryTierRepository.save(entity);
    }

    @Override
    public Function<CarryTierModel, CarryTier> toEntity() {
        return carryTierModel -> carryTierRepository.findById(carryTierModel.getId())
                .orElseThrow(() -> new EntityUnknownException(carryTierModel.getId()));
    }

    @Override
    public Function<CarryTier, CarryTierModel> toModel() {
        return CarryTier::toModel;
    }

    public Optional<CarryTier> findByCategory(Server server, long categoryId) {
        return carryTierRepository.findFirstByCarryType_ServerAndCategory(server, categoryId);
    }
}