package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.reputation.ReputationModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@Getter
@Entity(name = "reputation")
@Table(name = "reputation", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class Reputation implements net.dungeonhub.structure.entity.Entity<ReputationModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server_id", nullable = false)
    private DiscordServer discordServer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private DiscordUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reputor_id", nullable = false)
    private DiscordUser reputor;

    @Setter
    @Column(name = "rep_amount", nullable = false)
    private int amount;

    @Setter
    @Column(name = "rep_reason")
    private String reason;

    @Column(name = "time")
    private Instant time;

    public Reputation(DiscordServer discordServer, DiscordUser user, DiscordUser reputor, int amount, String reason, Instant time) {
        this.discordServer = discordServer;
        this.user = user;
        this.reputor = reputor;
        this.amount = amount;
        this.reason = reason;
        this.time = time;
    }

    @NotNull
    @Override
    public ReputationModel toModel() {
        return new ReputationModel(
                id,
                user.toModel(),
                reputor.toModel(),
                amount,
                reason,
                time
        );
    }
}