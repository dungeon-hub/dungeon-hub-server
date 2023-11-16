package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.server.ServerModel;

@Entity(name = "server")
@Table(name = "server", schema = "dungeon-hub")
@NoArgsConstructor
@AllArgsConstructor
public class Server implements EntityModelRelation<ServerModel> {
    @Getter
    @Setter
    @Id
    @Column(name = "id", nullable = false)
    //final
    private long id;

    @Override
    public Server fromModel(ServerModel model) {
        return new Server(model.getId());
    }

    @Override
    public ServerModel toModel() {
        return new ServerModel(id);
    }
}