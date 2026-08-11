package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@Getter
@Entity(name = "carry_difficulty_history")
@Table(name = "carry_difficulty_history", schema = "dungeon-hub")
@NoArgsConstructor
public class CarryDifficultyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "carry_difficulty", nullable = false)
    private CarryDifficulty carryDifficulty;

    @Column(name = "price", nullable = false)
    private int price;

    @Nullable
    @Column(name = "bulk_price")
    private Integer bulkPrice;

    @Nullable
    @Column(name = "bulk_amount")
    private Integer bulkAmount;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "date_from", nullable = false)
    private Instant dateFrom;

    @Column(name = "date_to", nullable = false)
    private Instant dateTo;

    public CarryDifficultyHistory(CarryDifficulty carryDifficulty, int price, Integer bulkPrice, Integer bulkAmount,
                                  int score, Instant dateFrom, Instant dateTo) {
        this.carryDifficulty = carryDifficulty;
        this.price = price;
        this.bulkPrice = bulkPrice;
        this.bulkAmount = bulkAmount;
        this.score = score;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public boolean includes(Instant instant) {
        return !instant.isBefore(dateFrom) && instant.isBefore(dateTo);
    }
}
