package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import net.dungeonhub.model.discord_role.DiscordRoleCreationModel;
import net.dungeonhub.model.discord_role.DiscordRoleModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class DiscordRoleInitializeModel implements InitializeModel<DiscordRole, DiscordRoleModel, DiscordRoleCreationModel> {
    private final DiscordServer discordServer;
    private long id;
    private String nameSchema;
    private boolean verifiedRole;

    public DiscordRoleInitializeModel(DiscordServer discordServer) {
        this.discordServer = discordServer;
    }

    @Override
    public @NotNull DiscordRole toEntity() {
        return new DiscordRole(id, nameSchema, verifiedRole, discordServer);
    }

    @Override
    public @NotNull DiscordRoleInitializeModel fromCreationModel(DiscordRoleCreationModel creationModel) {
        return new DiscordRoleInitializeModel(discordServer, creationModel.getId(), creationModel.getNameSchema(),
                creationModel.getVerifiedRole());
    }
}