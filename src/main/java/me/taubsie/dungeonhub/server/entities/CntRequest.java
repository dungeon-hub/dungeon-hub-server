package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.cnt_request.CntRequestModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Getter
@Entity(name = "cnt_request")
@Table(name = "cnt_request", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class CntRequest implements net.dungeonhub.structure.entity.Entity<CntRequestModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "message_id", nullable = false)
    private long messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server_id", nullable = false)
    private DiscordServer discordServer;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordUser user;

    @Setter
    @Nullable
    @JoinColumn(name = "claimer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordUser claimer;

    @Column(name = "request_time", nullable = false)
    private Instant time;

    @Setter
    @Column(name = "coin_value", nullable = false, length = 50)
    private String coinValue;

    @Setter
    @Column(name = "description", nullable = false)
    private String description;

    @Setter
    @Column(name = "requirement", nullable = false, length = 100)
    private String requirement;

    @Setter
    @Column(name = "completed", nullable = false)
    private boolean completed;

    public CntRequest(long messageId, DiscordServer discordServer, DiscordUser user, @Nullable DiscordUser claimer, Instant time, String coinValue, String description, String requirement, boolean completed) {
        this.messageId = messageId;
        this.discordServer = discordServer;
        this.user = user;
        this.claimer = claimer;
        this.time = time;
        this.coinValue = coinValue;
        this.description = description;
        this.requirement = requirement;
        this.completed = completed;
    }

    @Override
    public @NotNull CntRequestModel toModel() {
        return new CntRequestModel(id, messageId, discordServer.toModel(), user.toModel(), claimer != null ? claimer.toModel() : null, time, coinValue, description, requirement, completed);
    }
}