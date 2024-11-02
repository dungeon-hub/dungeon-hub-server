package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordRoleGroup;
import net.dungeonhub.model.discord_role_group.DiscordRoleGroupCreationModel;
import net.dungeonhub.model.discord_role_group.DiscordRoleGroupModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class DiscordRoleGroupInitializeModel implements InitializeModel<DiscordRoleGroup, DiscordRoleGroupModel, DiscordRoleGroupCreationModel> {
    private DiscordRole discordRole;
    private DiscordRole roleGroup;

    @Override
    public @NotNull DiscordRoleGroup toEntity() {
        return new DiscordRoleGroup(discordRole, roleGroup);
    }

    @Override
    public @NotNull DiscordRoleGroupInitializeModel fromCreationModel(DiscordRoleGroupCreationModel creationModel) {
        return new DiscordRoleGroupInitializeModel(discordRole, roleGroup);
    }
}