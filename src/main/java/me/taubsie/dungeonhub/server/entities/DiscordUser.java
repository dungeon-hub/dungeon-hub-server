package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.discord_user.DiscordUserModel;

import java.util.UUID;

@Getter
@Entity(name = "discord_user")
@Table(name = "discord_user", schema = "dungeon-hub")
@NoArgsConstructor
public class DiscordUser implements EntityModelRelation<DiscordUserModel> {
    @Id
    @Column(name = "id", nullable = false)
    private long id;

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
    public DiscordUser fromModel(DiscordUserModel model) {
        return new DiscordUser(model.getId(), model.getMinecraftId());
    }

    @Override
    public DiscordUserModel toModel() {
        return new DiscordUserModel(id, minecraftId);
    }
}