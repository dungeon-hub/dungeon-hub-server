package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.discord_role.DiscordRoleCreationModel;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.Server;

@Getter
@Setter
@AllArgsConstructor
public class DiscordRoleInitializeModel implements InitializeModel<DiscordRole, DiscordRoleCreationModel> {
    private final Server server;
    private long id;
    private String nameSchema;
    private Long roleGroup;
    private boolean verifiedRole;

    public DiscordRoleInitializeModel(Server server) {
        this.server = server;
    }

    @Override
    public DiscordRole toEntity() {
        return new DiscordRole(id, nameSchema, roleGroup, verifiedRole, server);
    }

    @Override
    public DiscordRoleInitializeModel fromCreationModel(DiscordRoleCreationModel creationModel) {
        return new DiscordRoleInitializeModel(server, creationModel.getId(), creationModel.getNameSchema(),
                creationModel.getRoleGroup(), creationModel.isVerifiedRole());
    }
}