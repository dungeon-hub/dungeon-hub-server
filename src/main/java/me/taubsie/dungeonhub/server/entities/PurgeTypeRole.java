package me.taubsie.dungeonhub.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.PurgeTypeRoleModel;

@Getter
@Entity(name = "purge_type_role")
@Table(name = "purge_type_role", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class PurgeTypeRole implements EntityModelRelation<PurgeTypeRoleModel> {
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
    public PurgeTypeRole fromModel(PurgeTypeRoleModel model) {
        PurgeTypeRoleId purgeTypeRoleId = new PurgeTypeRoleId();
        purgeTypeRoleId.setPurgeType(model.getPurgeTypeModel().getId());
        purgeTypeRoleId.setDiscordRole(model.getDiscordRoleModel().getId());

        return new PurgeTypeRole(purgeTypeRoleId, discordRole.fromModel(model.getDiscordRoleModel()), purgeType.fromModel(model.getPurgeTypeModel()));
    }

    @Override
    public PurgeTypeRoleModel toModel() {
        return new PurgeTypeRoleModel(null, discordRole.toModel());
    }
}