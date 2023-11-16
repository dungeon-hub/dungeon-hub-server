package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.server.ServerModel;
import me.taubsie.dungeonhub.server.entities.Server;

@AllArgsConstructor
@Getter
@Setter
public class ServerInitializeModel implements InitializeModel<Server, ServerModel> {
    private long id;

    @Override
    public Server toEntity() {
        return new Server(id);
    }

    @Override
    public ServerInitializeModel fromCreationModel(ServerModel creationModel) {
        return new ServerInitializeModel(creationModel.getId());
    }
}