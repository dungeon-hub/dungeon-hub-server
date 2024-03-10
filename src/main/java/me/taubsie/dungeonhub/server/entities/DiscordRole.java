package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.discord_role.DiscordRoleModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

@Entity(name = "discord_role")
@Table(name = "discord_role", schema = "dungeon-hub")
@NoArgsConstructor
public class DiscordRole implements EntityModelRelation<DiscordRoleModel> {
    @Id
    private long id;

    @Column(name = "name_schema")
    private String nameSchema;

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
    public @NotNull DiscordRole fromModel(@NotNull DiscordRoleModel model) {
        return new DiscordRole(model.getId(), model.getNameSchema(), model.isVerifiedRole(),
                discordServer.fromModel(model.getDiscordServerModel()));
    }

    @Override
    public @NotNull DiscordRoleModel toModel() {
        return new DiscordRoleModel(id, nameSchema, verifiedRole, discordServer.toModel());
    }
}