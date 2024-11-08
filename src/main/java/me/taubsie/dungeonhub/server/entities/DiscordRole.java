package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.discord_role.DiscordRoleModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

@Entity(name = "discord_role")
@Table(name = "discord_role", schema = "dungeon-hub")
@NoArgsConstructor
public class DiscordRole implements net.dungeonhub.structure.entity.Entity<DiscordRoleModel> {
    @Id
    private long id;

    @Setter
    @Column(name = "name_schema")
    private String nameSchema;

    @Setter
    @Column(name = "verified_role")
    private boolean verifiedRole;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server", nullable = false)
    private DiscordServer discordServer;

    public DiscordRole(long id, String nameSchema, boolean verifiedRole, DiscordServer discordServer) {
        this.id = id;
        this.nameSchema = nameSchema;
        this.verifiedRole = verifiedRole;
        this.discordServer = discordServer;
    }

    @Override
    public @NotNull DiscordRoleModel toModel() {
        return new DiscordRoleModel(id, nameSchema, verifiedRole, discordServer.toModel());
    }
}