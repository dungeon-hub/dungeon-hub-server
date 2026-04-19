package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.RoleRequirement;
import me.taubsie.dungeonhub.server.model.RoleRequirementInitializeModel;
import me.taubsie.dungeonhub.server.repositories.RoleRequirementRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.role_requirement.RoleRequirementCreationModel;
import net.dungeonhub.model.role_requirement.RoleRequirementModel;
import net.dungeonhub.model.role_requirement.RoleRequirementUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class RoleRequirementService implements EntityService<RoleRequirement, RoleRequirementModel, RoleRequirementCreationModel, RoleRequirementInitializeModel, RoleRequirementUpdateModel> {
    private final RoleRequirementRepository roleRequirementRepository;

    @Autowired
    public RoleRequirementService(RoleRequirementRepository roleRequirementRepository) {
        this.roleRequirementRepository = roleRequirementRepository;
    }

    @NotNull
    @Override
    public Optional<RoleRequirement> loadEntityById(long id) {
        return roleRequirementRepository.findById(id);
    }

    public Optional<RoleRequirement> loadEntityById(DiscordServer discordServer, long id) {
        return roleRequirementRepository.findById(id)
                .filter(roleRequirement -> roleRequirement.getDiscordRole().getDiscordServer().getId() == discordServer.getId());
    }

    @NotNull
    @Override
    public List<RoleRequirement> findAllEntities() {
        return roleRequirementRepository.findAll();
    }

    public List<RoleRequirement> loadEntitiesByDiscordServer(DiscordServer discordServer) {
        return roleRequirementRepository.findRoleRequirementsByDiscordRole_DiscordServer(discordServer);
    }

    @NotNull
    @Override
    public RoleRequirement createEntity(@NotNull RoleRequirementInitializeModel initializeModel) {
        return saveEntity(initializeModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return roleRequirementRepository.findById(id).map(entity ->
        {
            roleRequirementRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    public void delete(RoleRequirement roleRequirement) {
        roleRequirementRepository.delete(roleRequirement);
    }

    @NotNull
    @Override
    public RoleRequirement saveEntity(@NotNull RoleRequirement roleRequirement) {
        return roleRequirementRepository.save(roleRequirement);
    }

    @Nullable
    @Override
    public Function<RoleRequirementModel, RoleRequirement> toEntity() {
        return roleRequirementModel -> roleRequirementRepository.findById(roleRequirementModel.getId()).orElseThrow(() -> new EntityUnknownException(roleRequirementModel.getId()));
    }

    @NotNull
    @Override
    public Function<RoleRequirement, RoleRequirementModel> toModel() {
        return RoleRequirement::toModel;
    }

    @NotNull
    @Override
    public RoleRequirement updateEntity(@NotNull RoleRequirement roleRequirement, @NotNull RoleRequirementUpdateModel updateModel) {
        if (updateModel.getComparison() != null) {
            roleRequirement.setComparison(updateModel.getComparison());
        }

        if (updateModel.getCount() != null) {
            roleRequirement.setCount(updateModel.getCount());
        }

        if (updateModel.getExtraData() != null) {
            roleRequirement.setExtraData(updateModel.getExtraData());
        }

        if (updateModel.getResetExtraData()) {
            roleRequirement.setExtraData(null);
        }

        return roleRequirement;
    }
}