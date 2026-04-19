package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.score.ScoreModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Entity
@Table(name = "score", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class Score implements net.dungeonhub.structure.entity.Entity<ScoreModel> {
    @EmbeddedId
    private ScoreId id;

    @MapsId("carryType")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carry_type")
    private CarryType carryType;

    @MapsId("id")
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id", nullable = false)
    private DiscordUser carrier;

    @Column(name = "score")
    private Long scoreAmount;

    public void setScoreAmount(Long scoreAmount) {
        if (scoreAmount < 0L) {
            scoreAmount = 0L;
        }

        this.scoreAmount = scoreAmount;
    }

    @Override
    public @NotNull ScoreModel toModel() {
        return new ScoreModel(carrier.toModel(), carryType.toModel(), id.getScoreType(), scoreAmount);
    }
}