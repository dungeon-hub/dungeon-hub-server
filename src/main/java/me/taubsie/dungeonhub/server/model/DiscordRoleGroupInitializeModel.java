package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.discord_role_group.DiscordRoleGroupCreationModel;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordRoleGroup;

@AllArgsConstructor
@Getter
public class DiscordRoleGroupInitializeModel implements InitializeModel<DiscordRoleGroup, DiscordRoleGroupCreationModel> {
    private DiscordRole discordRole;
    private DiscordRole roleGroup;

    @Override
    public DiscordRoleGroup toEntity() {
        return new DiscordRoleGroup(discordRole, roleGroup);
    }

    @Override
    public DiscordRoleGroupInitializeModel fromCreationModel(DiscordRoleGroupCreationModel creationModel) {
        return new DiscordRoleGroupInitializeModel(discordRole.fromModel(creationModel.getDiscordRole()), roleGroup.fromModel(creationModel.getRoleGroup()));
    }
}