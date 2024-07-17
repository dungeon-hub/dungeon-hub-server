package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.enums.WarningType;
import me.taubsie.dungeonhub.common.model.warning.DetailedWarningModel;
import me.taubsie.dungeonhub.common.model.warning.WarningModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "warns", schema = "dungeon-hub")
@NoArgsConstructor
public class Warning implements EntityModelRelation<WarningModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @JoinColumn(name = "server_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordServer server;

    @JoinColumn(name = "warned_user", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordUser user;

    @JoinColumn(name = "striker", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordUser striker;

    @Enumerated
    @Column(name = "warning_type", nullable = false)
    private WarningType warningType;

    @Nullable
    @Column(name = "reason")
    private String reason;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "time", nullable = false)
    private Instant time;

    @OneToMany(mappedBy = "warning")
    private List<WarningEvidence> evidences;

    public Warning(long id, DiscordServer server, DiscordUser user, DiscordUser striker, WarningType warningType, @Nullable String reason, boolean active, Instant time) {
        this.id = id;
        this.server = server;
        this.user = user;
        this.striker = striker;
        this.warningType = warningType;
        this.reason = reason;
        this.active = active;
        this.time = time;
    }

    public Warning(DiscordServer server, DiscordUser user, DiscordUser striker, WarningType warningType, @Nullable String reason, boolean active, Instant time) {
        this.server = server;
        this.user = user;
        this.striker = striker;
        this.warningType = warningType;
        this.reason = reason;
        this.active = active;
        this.time = time;
    }

    @Override
    public @NotNull Warning fromModel(@NotNull WarningModel model) {
        return new Warning(model.getId(), server.fromModel(model.getServer()), user.fromModel(model.getUser()), striker.fromModel(model.getStriker()), model.getWarningType(), model.getReason(), model.isActive(), model.getTime());
    }

    @Override
    public @NotNull WarningModel toModel() {
        return new WarningModel(id, server.toModel(), user.toModel(), striker.toModel(), warningType, reason, active, time);
    }

    public @NotNull DetailedWarningModel toDetailedModel() {
        return new DetailedWarningModel(id, server.toModel(), user.toModel(), striker.toModel(), warningType, reason, active, time, evidences.stream().map(WarningEvidence::toModel).toList());
    }
}