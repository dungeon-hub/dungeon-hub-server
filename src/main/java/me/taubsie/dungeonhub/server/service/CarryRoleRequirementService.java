package me.taubsie.dungeonhub.server.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.CarryRoleRequirementCreationModel;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.CarryRoleRequirementModel;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.CarryRoleRequirementUpdateModel;
import me.taubsie.dungeonhub.server.entities.CarryRoleRequirement;
import me.taubsie.dungeonhub.server.model.CarryRoleRequirementInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryRoleRequirementRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor @Getter(AccessLevel.PROTECTED) @Service
public class CarryRoleRequirementService implements EntityService<CarryRoleRequirement, CarryRoleRequirementModel, CarryRoleRequirementCreationModel, CarryRoleRequirementInitializeModel, CarryRoleRequirementUpdateModel> {

    private final CarryRoleRequirementRepository requirementRepository;

    @Override
    public Optional<CarryRoleRequirement> loadEntityById(long id) {
        return getRequirementRepository().findById(id);
    }

    @Override
    public Optional<CarryRoleRequirement> loadEntityByName(String name) {
        return Optional.empty();
    }

    @Override
    public List<CarryRoleRequirement> findAllEntities() {
        return getRequirementRepository().findAll();
    }

    @Override
    public CarryRoleRequirement createEntity(@NotNull CarryRoleRequirementInitializeModel initializationModel) {
        return saveEntity(initializationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return getRequirementRepository().findById(id).map(entity -> {
            getRequirementRepository().delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public CarryRoleRequirement saveEntity(@NotNull CarryRoleRequirement entity) {
        return getRequirementRepository().save(entity);
    }

    @Override
    public Function<CarryRoleRequirementModel, CarryRoleRequirement> toEntity() {
        return (model) -> getRequirementRepository().getReferenceById(model.getId());
    }

    @Override
    public Function<CarryRoleRequirement, CarryRoleRequirementModel> toModel() {
        return CarryRoleRequirement::toModel;
    }
}
