package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordRoleGroup;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordRoleGroupInitializeModel;
import me.taubsie.dungeonhub.server.repositories.DiscordRoleGroupRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.discord_role_group.DiscordRoleGroupCreationModel;
import net.dungeonhub.model.discord_role_group.DiscordRoleGroupModel;
import net.dungeonhub.model.discord_role_group.DiscordRoleGroupUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class DiscordRoleGroupService implements EntityService<DiscordRoleGroup, DiscordRoleGroupModel, DiscordRoleGroupCreationModel, DiscordRoleGroupInitializeModel, DiscordRoleGroupUpdateModel> {
    private final DiscordRoleGroupRepository discordRoleGroupRepository;

    @Override
    public @NotNull Optional<DiscordRoleGroup> loadEntityById(long id) {
        return discordRoleGroupRepository.findById(id);
    }

    public Optional<DiscordRoleGroup> loadEntityById(DiscordServer discordServer, long id) {
        return discordRoleGroupRepository.findById(id)
                .filter(discordRoleGroup -> discordRoleGroup.getDiscordRole().getDiscordServer().getId() == discordServer.getId());
    }

    @Override
    public @NotNull List<DiscordRoleGroup> findAllEntities() {
        return discordRoleGroupRepository.findAll();
    }

    @Override
    public @NotNull DiscordRoleGroup createEntity(DiscordRoleGroupInitializeModel initalizationModel) {
        return discordRoleGroupRepository.save(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return discordRoleGroupRepository.findById(id).map(discordRoleGroup ->
        {
            discordRoleGroupRepository.delete(discordRoleGroup);
            return true;
        }).orElse(false);
    }

    public void delete(DiscordRoleGroup discordRoleGroup) {
        discordRoleGroupRepository.delete(discordRoleGroup);
    }

    @Override
    public @NotNull DiscordRoleGroup saveEntity(@NotNull DiscordRoleGroup entity) {
        return discordRoleGroupRepository.save(entity);
    }

    @Override
    public Function<DiscordRoleGroupModel, DiscordRoleGroup> toEntity() {
        return discordRoleGroupModel -> loadEntityById(discordRoleGroupModel.getId())
                .orElseThrow(() -> new EntityUnknownException(discordRoleGroupModel.getId()));
    }

    @Override
    public @NotNull Function<DiscordRoleGroup, DiscordRoleGroupModel> toModel() {
        return DiscordRoleGroup::toModel;
    }

    public List<DiscordRoleGroup> loadEntitiesByDiscordServer(DiscordServer discordServer) {
        return discordRoleGroupRepository.findDiscordRoleGroupsByDiscordRole_DiscordServer(discordServer);
    }

    @Override
    public @NotNull DiscordRoleGroup updateEntity(@NotNull DiscordRoleGroup discordRoleGroup, @NotNull DiscordRoleGroupUpdateModel discordRoleGroupUpdateModel) {
        return discordRoleGroup;
    }
}