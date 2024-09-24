package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.cnt_request.CntRequestModel;
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
public class CntRequest implements EntityModelRelation<CntRequestModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "message_id", nullable = false)
    private long messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server", nullable = false)
    private DiscordServer discordServer;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordUser user;

    @Nullable
    @JoinColumn(name = "claimer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordUser claimer;

    @Column(name = "request_time", nullable = false)
    private Instant time;

    @Column(name = "coin_value", nullable = false, length = 50)
    private String coinValue;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "requirement", nullable = false, length = 100)
    private String requirement;

    public CntRequest(long messageId, DiscordServer discordServer, DiscordUser user, @Nullable DiscordUser claimer, Instant time, String coinValue, String description, String requirement) {
        this.messageId = messageId;
        this.discordServer = discordServer;
        this.user = user;
        this.claimer = claimer;
        this.time = time;
        this.coinValue = coinValue;
        this.description = description;
        this.requirement = requirement;
    }

    @Override
    public @NotNull CntRequest fromModel(@NotNull CntRequestModel model) {
        return new CntRequest(
                model.getId(),
                model.getMessageId(),
                discordServer.fromModel(model.getDiscordServer()),
                user.fromModel(model.getUser()),
                //TODO better handling if claimer in the model is null :(
                model.getClaimer() != null ? (
                        claimer != null ? claimer.fromModel(model.getClaimer()) : user.fromModel(model.getClaimer())
                ) : null,
                model.getTime(),
                model.getCoinValue(),
                model.getDescription(),
                model.getRequirement()
        );
    }

    @Override
    public @NotNull CntRequestModel toModel() {
        return new CntRequestModel(id, messageId, discordServer.toModel(), user.toModel(), claimer != null ? claimer.toModel() : null, time, coinValue, description, requirement);
    }
}