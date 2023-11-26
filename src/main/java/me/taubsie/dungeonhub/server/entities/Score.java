package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.score.ScoreModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "score", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class Score implements EntityModelRelation<ScoreModel> {
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
        if(scoreAmount < 0L) {
            scoreAmount = 0L;
        }

        this.scoreAmount = scoreAmount;
    }

    @Override
    public Score fromModel(ScoreModel model) {
        ScoreId scoreId = new ScoreId();
        scoreId.setId(model.getCarrier().getId());
        scoreId.setScoreType(model.getScoreType());
        scoreId.setCarryType(model.getCarryType().getId());

        Score score = new Score();
        score.setId(scoreId);
        score.setScoreAmount(model.getScoreAmount());
        score.setCarryType(carryType.fromModel(model.getCarryType()));
        score.setCarrier(carrier.fromModel(model.getCarrier()));

        return score;
    }

    @Override
    public ScoreModel toModel() {
        return new ScoreModel(carrier.toModel(), carryType.toModel(), id.getScoreType(), scoreAmount);
    }
}