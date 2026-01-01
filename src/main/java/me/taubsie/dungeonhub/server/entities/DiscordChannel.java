package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.discord_channel.DiscordChannelModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jspecify.annotations.NonNull;

@Entity(name = "discord_channel")
@Table(name = "discord_channel", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class DiscordChannel implements net.dungeonhub.structure.entity.Entity<DiscordChannelModel> {
    @Id
    private long id;

    @Setter
    @Column(name = "name")
    private String name;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server", nullable = false)
    private DiscordServer discordServer;

    @Getter
    @Setter
    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Override
    public @NonNull DiscordChannelModel toModel() {
        return new DiscordChannelModel(id, name, discordServer.toModel(), deleted);
    }
}