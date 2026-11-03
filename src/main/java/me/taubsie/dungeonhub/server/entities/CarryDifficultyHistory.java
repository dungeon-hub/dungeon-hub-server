package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dungeonhub.model.carry_difficulty.BulkPriceModel;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The pricing values a {@link CarryDifficulty} had during a closed period of time.
 *
 * <p>A row is written whenever the price, bulk prices or score of a difficulty changes. It stores the
 * replaced values together with the period they were valid in. The values that are currently valid are not part of
 * the history; they live on the {@link CarryDifficulty} itself.</p>
 */
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "carry_difficulty_history_bulk_price", schema = "dungeon-hub",
            joinColumns = @JoinColumn(name = "carry_difficulty_history", nullable = false))
    @OrderBy("amount")
    private List<CarryDifficultyHistoryBulkPrice> bulkPrices = new ArrayList<>();

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "date_from", nullable = false)
    private Instant dateFrom;

    @Column(name = "date_to", nullable = false)
    private Instant dateTo;

    public CarryDifficultyHistory(CarryDifficulty carryDifficulty, int price,
                                  List<CarryDifficultyHistoryBulkPrice> bulkPrices, int score, Instant dateFrom, Instant dateTo) {
        this.carryDifficulty = carryDifficulty;
        this.price = price;
        this.bulkPrices = bulkPrices == null ? new ArrayList<>() : new ArrayList<>(bulkPrices);
        this.score = score;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    /**
     * @return whether {@code instant} lies in this period; {@code dateFrom} is inclusive, {@code dateTo} is exclusive
     */
    public boolean includes(Instant instant) {
        return !instant.isBefore(dateFrom) && instant.isBefore(dateTo);
    }

    public long calculateTotalPrice(int amount) {
        List<BulkPriceModel> bulkPriceModels = bulkPrices.stream()
                .sorted(Comparator.comparingInt(CarryDifficultyHistoryBulkPrice::getAmount))
                .map(CarryDifficultyHistoryBulkPrice::toModel)
                .toList();

        return CarryDifficultyModel.Companion.calculateTotalPrice(amount, bulkPriceModels, price);
    }
}
