package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dungeonhub.model.discord_role_group.DiscordRoleGroupModel;
import org.jetbrains.annotations.NotNull;

@Entity(name = "discord_role_group")
@Table(name = "discord_role_group", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DiscordRoleGroup implements net.dungeonhub.structure.entity.Entity<DiscordRoleGroupModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discord_role", nullable = false)
    private DiscordRole discordRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_group", nullable = false)
    private DiscordRole roleGroup;

    public DiscordRoleGroup(DiscordRole discordRole, DiscordRole roleGroup) {
        this.discordRole = discordRole;
        this.roleGroup = roleGroup;
    }

    @Override
    public @NotNull DiscordRoleGroupModel toModel() {
        return new DiscordRoleGroupModel(id, discordRole.toModel(), roleGroup.toModel());
    }
}