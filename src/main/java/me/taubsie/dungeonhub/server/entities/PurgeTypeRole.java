package me.taubsie.dungeonhub.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dungeonhub.model.purge_type.PurgeTypeRoleModel;
import org.jetbrains.annotations.NotNull;

@Getter
@Entity(name = "purge_type_role")
@Table(name = "purge_type_role", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class PurgeTypeRole implements net.dungeonhub.structure.entity.Entity<PurgeTypeRoleModel> {
    @EmbeddedId
    private PurgeTypeRoleId id;

    @MapsId("discordRole")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discord_role")
    private DiscordRole discordRole;

    @MapsId("purgeType")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purge_type")
    @JsonIgnore
    private PurgeType purgeType;

    @Override
    public @NotNull PurgeTypeRoleModel toModel() {
        return new PurgeTypeRoleModel(purgeType.toSimpleModel(), discordRole.toModel());
    }
}