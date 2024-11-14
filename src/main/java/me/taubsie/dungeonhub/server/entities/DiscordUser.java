package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.discord_user.DiscordUserModel;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Entity(name = "discord_user")
@Table(name = "discord_user", schema = "dungeon-hub")
@NoArgsConstructor
public class DiscordUser implements net.dungeonhub.structure.entity.Entity<DiscordUserModel>, Serializable {
    @Serial
    private static final long serialVersionUID = 1674869422651134221L;

    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Setter
    @Column(name = "minecraft_id")
    private UUID minecraftId;

    public DiscordUser(long id) {
        this.id = id;
    }

    public DiscordUser(long id, UUID minecraftId) {
        this.id = id;
        this.minecraftId = minecraftId;
    }

    @Override
    public @NotNull DiscordUserModel toModel() {
        return new DiscordUserModel(id, minecraftId);
    }
}