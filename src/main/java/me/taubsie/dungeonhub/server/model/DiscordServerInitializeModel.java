package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.server.DiscordServerModel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;

@AllArgsConstructor
@Getter
@Setter
public class DiscordServerInitializeModel implements InitializeModel<DiscordServer, DiscordServerModel> {
    private long id;

    @Override
    public DiscordServer toEntity() {
        return new DiscordServer(id);
    }

    @Override
    public DiscordServerInitializeModel fromCreationModel(DiscordServerModel creationModel) {
        return new DiscordServerInitializeModel(creationModel.getId());
    }
}