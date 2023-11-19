package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.discord_user.DiscordUserCreationModel;
import me.taubsie.dungeonhub.server.entities.DiscordUser;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiscordUserInitializeModel implements InitializeModel<DiscordUser, DiscordUserCreationModel> {
    private long id;
    private UUID minecraftId;

    @Override
    public DiscordUser toEntity() {
        return new DiscordUser(id, minecraftId);
    }

    @Override
    public DiscordUserInitializeModel fromCreationModel(DiscordUserCreationModel creationModel) {
        return new DiscordUserInitializeModel(creationModel.getId(), creationModel.getMinecraftId());
    }
}