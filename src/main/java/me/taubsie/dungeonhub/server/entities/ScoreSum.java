package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import net.dungeonhub.model.score.ScoreModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "score_sum", schema = "dungeon-hub")
public class ScoreSum {
    @EmbeddedId
    private ScoreSumId id;

    @MapsId("server")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server")
    private DiscordServer server;

    @MapsId("id")
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id", nullable = false)
    private DiscordUser carrier;

    private long totalScore;

    public ScoreModel toScoreModel() {
        return new ScoreModel(carrier.toModel(), null, id.getScoreType(), totalScore);
    }
}