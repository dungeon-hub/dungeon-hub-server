package me.taubsie.dungeonhub.server.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.model.carryrole.CarryRoleCreationModel;
import me.taubsie.dungeonhub.common.model.carryrole.CarryRoleModel;
import me.taubsie.dungeonhub.common.model.carryrole.CarryRoleUpdateModel;
import me.taubsie.dungeonhub.server.entities.CarryRole;
import me.taubsie.dungeonhub.server.model.CarryRoleInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryRoleRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Service
public class CarryRoleService implements EntityService<CarryRole, CarryRoleModel, CarryRoleCreationModel, CarryRoleInitializeModel, CarryRoleUpdateModel> {

    private final CarryRoleRepository roleRepository;

    @Override
    public Optional<CarryRole> loadEntityById(long id) {
        return getRoleRepository().findById(id);
    }

    @Override
    public Optional<CarryRole> loadEntityByName(String name) {
        return getRoleRepository().findByDisplayName(name);
    }

    @Override
    public List<CarryRole> findAllEntities() {
        return getRoleRepository().findAll();
    }

    @Override
    public @NotNull CarryRole createEntity(@NotNull CarryRoleInitializeModel initializationModel) {
        return saveEntity(initializationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return getRoleRepository().findById(id).map(entity -> {
            getRoleRepository().delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public @NotNull CarryRole saveEntity(@NotNull CarryRole entity) {
        return getRoleRepository().save(entity);
    }

    @Override
    public Function<CarryRoleModel, CarryRole> toEntity() {
        return (model) -> getRoleRepository().getReferenceById(model.getId());
    }

    @Override
    public Function<CarryRole, CarryRoleModel> toModel() {
        return CarryRole::toModel;
    }
}
