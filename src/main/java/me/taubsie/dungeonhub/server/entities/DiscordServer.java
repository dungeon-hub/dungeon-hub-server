package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.server.DiscordServerModel;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@Entity(name = "server")
@Table(name = "server", schema = "dungeon-hub")
@NoArgsConstructor
@AllArgsConstructor
public class DiscordServer implements EntityModelRelation<DiscordServerModel> {
    @Id
    @Column(name = "id", nullable = false)
    //final
    private long id;

    @Override
    public @NotNull DiscordServer fromModel(@NotNull DiscordServerModel model) {
        return new DiscordServer(model.getId());
    }

    @Override
    public @NotNull DiscordServerModel toModel() {
        return new DiscordServerModel(id);
    }
}