package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordRoleInitializeModel;
import me.taubsie.dungeonhub.server.repositories.DiscordRoleRepository;
import net.dungeonhub.expections.EntityUnknownException;
import net.dungeonhub.model.discord_role.DiscordRoleCreationModel;
import net.dungeonhub.model.discord_role.DiscordRoleModel;
import net.dungeonhub.model.discord_role.DiscordRoleUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class DiscordRoleService implements EntityService<DiscordRole, DiscordRoleModel, DiscordRoleCreationModel,
        DiscordRoleInitializeModel, DiscordRoleUpdateModel> {
    private final DiscordRoleRepository discordRoleRepository;

    @Override
    public @NotNull Optional<DiscordRole> loadEntityById(long id) {
        return discordRoleRepository.findById(id);
    }

    public Optional<DiscordRole> loadEntityById(DiscordServer discordServer, long id) {
        return discordRoleRepository.findById(id)
                .filter(discordRole -> discordRole.getDiscordServer().getId() == discordServer.getId());
    }

    public DiscordRole loadOrCreate(DiscordServer discordServer, long id) {
        return loadEntityById(discordServer, id)
                .orElseGet(() -> createEntity(new DiscordRoleInitializeModel(discordServer)
                        .fromCreationModel(new DiscordRoleCreationModel(id, null, RoleAction.None))));
    }

    @Override
    public @NotNull List<DiscordRole> findAllEntities() {
        return discordRoleRepository.findAll();
    }

    @Override
    public @NotNull DiscordRole createEntity(DiscordRoleInitializeModel initalizationModel) {
        return discordRoleRepository.save(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return discordRoleRepository.findById(id).map(discordRole ->
        {
            discordRoleRepository.delete(discordRole);
            return true;
        }).orElse(false);
    }

    public void delete(DiscordRole discordRole) {
        discordRoleRepository.delete(discordRole);
    }

    @Override
    public @NotNull DiscordRole saveEntity(@NotNull DiscordRole entity) {
        return discordRoleRepository.save(entity);
    }

    @Override
    public Function<DiscordRoleModel, DiscordRole> toEntity() {
        return discordRoleModel -> loadEntityById(discordRoleModel.getId())
                .orElseThrow(() -> new EntityUnknownException(discordRoleModel.getId()));
    }

    @Override
    public @NotNull Function<DiscordRole, DiscordRoleModel> toModel() {
        return DiscordRole::toModel;
    }

    public List<DiscordRole> loadEntitiesByDiscordServer(DiscordServer discordServer) {
        return discordRoleRepository.findDiscordRolesByDiscordServer(discordServer);
    }

    @Override
    public @NotNull DiscordRole updateEntity(@NotNull DiscordRole discordRole, @NotNull DiscordRoleUpdateModel discordRoleUpdateModel) {
        if (discordRoleUpdateModel.getResetNameSchema()) {
            discordRole.setNameSchema(null);
        }

        if (discordRoleUpdateModel.getNameSchema() != null) {
            discordRole.setNameSchema(discordRoleUpdateModel.getNameSchema());
        }

        if (discordRoleUpdateModel.getRoleAction() != null) {
            discordRole.setRoleAction(discordRoleUpdateModel.getRoleAction());
        }

        return discordRole;
    }
}