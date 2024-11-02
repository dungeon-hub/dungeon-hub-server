package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.discord_server.DiscordServerModel;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@Entity(name = "server")
@Table(name = "server", schema = "dungeon-hub")
@NoArgsConstructor
@AllArgsConstructor
public class DiscordServer implements net.dungeonhub.structure.entity.Entity<DiscordServerModel> {
    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Override
    public @NotNull DiscordServerModel toModel() {
        return new DiscordServerModel(id);
    }
}