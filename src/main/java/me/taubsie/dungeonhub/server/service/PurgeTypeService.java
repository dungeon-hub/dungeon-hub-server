package me.taubsie.dungeonhub.server.service;

import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.PurgeType;
import me.taubsie.dungeonhub.server.model.PurgeTypeInitializeModel;
import me.taubsie.dungeonhub.server.repositories.PurgeTypeRepository;
import net.dungeonhub.expections.EntityUnknownException;
import net.dungeonhub.model.purge_type.PurgeTypeCreationModel;
import net.dungeonhub.model.purge_type.PurgeTypeModel;
import net.dungeonhub.model.purge_type.PurgeTypeUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@NoArgsConstructor
public class PurgeTypeService implements EntityService<PurgeType, PurgeTypeModel, PurgeTypeCreationModel, PurgeTypeInitializeModel, PurgeTypeUpdateModel> {
    private PurgeTypeRepository purgeTypeRepository;

    @Autowired
    public PurgeTypeService(PurgeTypeRepository purgeTypeRepository) {
        this.purgeTypeRepository = purgeTypeRepository;
    }

    @Override
    public @NotNull Optional<PurgeType> loadEntityById(long id) {
        return purgeTypeRepository.findById(id);
    }

    public Optional<PurgeType> loadEntityById(CarryType carryType, long id) {
        return purgeTypeRepository.findById(id)
                .filter(purgeType -> purgeType.getCarryType().getId() == carryType.getId());
    }

    public List<PurgeType> loadEntitiesByCarryType(CarryType carryType) {
        return purgeTypeRepository.findPurgeTypesByCarryType(carryType);
    }

    @Override
    public @NotNull List<PurgeType> findAllEntities() {
        return purgeTypeRepository.findAll();
    }

    @Override
    public @NotNull PurgeType createEntity(PurgeTypeInitializeModel initalizationModel) {
        return saveEntity(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return purgeTypeRepository.findById(id).map(entity ->
        {
            purgeTypeRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public @NotNull PurgeType saveEntity(@NotNull PurgeType entity) {
        return purgeTypeRepository.save(entity);
    }

    @Override
    public Function<PurgeTypeModel, PurgeType> toEntity() {
        return purgeTypeModel -> purgeTypeRepository.findById(purgeTypeModel.getId()).orElseThrow(() -> new EntityUnknownException(purgeTypeModel.getId()));
    }

    @Override
    public @NotNull Function<PurgeType, PurgeTypeModel> toModel() {
        return PurgeType::toModel;
    }

    @Override
    public @NotNull PurgeType updateEntity(@NotNull PurgeType purgeType, @NotNull PurgeTypeUpdateModel purgeTypeUpdateModel) {
        if(purgeTypeUpdateModel.getDisplayName() != null) {
            purgeType.setDisplayName(purgeTypeUpdateModel.getDisplayName());
        }

        return purgeType;
    }
}