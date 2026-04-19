package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.DiscordChannel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import net.dungeonhub.model.discord_channel.DiscordChannelCreationModel;
import net.dungeonhub.model.discord_channel.DiscordChannelModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jspecify.annotations.NonNull;

@Getter
@Setter
@AllArgsConstructor
public class DiscordChannelInitializeModel implements InitializeModel<DiscordChannel, DiscordChannelModel, DiscordChannelCreationModel> {
    private final DiscordServer discordServer;
    private long id;
    private String name;
    private boolean deleted;

    public DiscordChannelInitializeModel(DiscordServer discordServer) {
        this.discordServer = discordServer;
    }

    @Override
    public @NonNull DiscordChannel toEntity() {
        return new DiscordChannel(id, name, discordServer, deleted);
    }

    @Override
    public @NonNull DiscordChannelInitializeModel fromCreationModel(@NonNull DiscordChannelCreationModel discordChannelCreationModel) {
        return new DiscordChannelInitializeModel(discordServer, discordChannelCreationModel.getId(), discordChannelCreationModel.getName(), false);
    }
}