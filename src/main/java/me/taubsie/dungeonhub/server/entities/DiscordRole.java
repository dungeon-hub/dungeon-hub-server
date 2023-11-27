package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.discord_role.DiscordRoleModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.Nullable;

@Entity(name = "discord_role")
@Table(name = "discord_role", schema = "dungeon-hub")
@NoArgsConstructor
public class DiscordRole implements EntityModelRelation<DiscordRoleModel> {
    @Id
    private long id;

    @Column(name = "name_schema")
    private String nameSchema;

    @Nullable
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "role_group")
    private Long roleGroup;

    @Column(name = "verified_role")
    private boolean verifiedRole;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server", nullable = false)
    private DiscordServer discordServer;

    public DiscordRole(long id, String nameSchema, @Nullable Long roleGroup, boolean verifiedRole, DiscordServer discordServer) {
        this.id = id;
        this.nameSchema = nameSchema;
        this.roleGroup = roleGroup;
        this.verifiedRole = verifiedRole;
        this.discordServer = discordServer;
    }

    @Override
    public DiscordRole fromModel(DiscordRoleModel model) {
        return new DiscordRole(model.getId(), model.getNameSchema(), model.getRoleGroup(), model.isVerifiedRole(),
                discordServer.fromModel(model.getDiscordServerModel()));
    }

    @Override
    public DiscordRoleModel toModel() {
        return new DiscordRoleModel(id, nameSchema, roleGroup, verifiedRole, discordServer.toModel());
    }
}