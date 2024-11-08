package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import net.dungeonhub.model.discord_user.DiscordUserCreationModel;
import net.dungeonhub.model.discord_user.DiscordUserModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiscordUserInitializeModel implements InitializeModel<DiscordUser, DiscordUserModel, DiscordUserCreationModel> {
    private long id;
    private UUID minecraftId;

    @Override
    public @NotNull DiscordUser toEntity() {
        return new DiscordUser(id, minecraftId);
    }

    @Override
    public @NotNull DiscordUserInitializeModel fromCreationModel(DiscordUserCreationModel creationModel) {
        return new DiscordUserInitializeModel(creationModel.getId(), creationModel.getMinecraftId());
    }
}