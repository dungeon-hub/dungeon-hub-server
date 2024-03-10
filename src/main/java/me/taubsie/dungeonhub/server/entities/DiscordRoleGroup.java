package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.discord_role_group.DiscordRoleGroupModel;
import org.jetbrains.annotations.NotNull;

@Entity(name = "discord_role_group")
@Table(name = "discord_role_group", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DiscordRoleGroup implements EntityModelRelation<DiscordRoleGroupModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discord_role")
    private DiscordRole discordRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_group", nullable = false)
    private DiscordRole roleGroup;

    public DiscordRoleGroup(DiscordRole discordRole, DiscordRole roleGroup) {
        this.discordRole = discordRole;
        this.roleGroup = roleGroup;
    }

    @Override
    public @NotNull DiscordRoleGroup fromModel(@NotNull DiscordRoleGroupModel model) {
        return new DiscordRoleGroup(model.getId(), discordRole.fromModel(model.getDiscordRole()), roleGroup.fromModel(model.getRoleGroup()));
    }

    @Override
    public @NotNull DiscordRoleGroupModel toModel() {
        return new DiscordRoleGroupModel(id, discordRole.toModel(), roleGroup.toModel());
    }
}