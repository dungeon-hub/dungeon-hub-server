package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.enums.RoleAction;
import me.taubsie.dungeonhub.common.model.discord_role.DiscordRoleCreationModel;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordServer;

@Getter
@Setter
@AllArgsConstructor
public class DiscordRoleInitializeModel implements InitializeModel<DiscordRole, DiscordRoleCreationModel> {
    private final DiscordServer discordServer;
    private long id;
    private String nameSchema;
    private RoleAction roleAction;

    public DiscordRoleInitializeModel(DiscordServer discordServer) {
        this.discordServer = discordServer;
    }

    @Override
    public DiscordRole toEntity() {
        return new DiscordRole(id, nameSchema, roleAction, discordServer);
    }

    @Override
    public DiscordRoleInitializeModel fromCreationModel(DiscordRoleCreationModel creationModel) {
        return new DiscordRoleInitializeModel(discordServer, creationModel.getId(), creationModel.getNameSchema(),
                creationModel.getRoleAction());
    }
}