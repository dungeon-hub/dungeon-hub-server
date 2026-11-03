package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dungeonhub.model.carry_difficulty.BulkPriceModel;

import java.util.List;

/**
 * A bulk price a {@link CarryDifficulty} had during the period of a {@link CarryDifficultyHistory}.
 */
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CarryDifficultyHistoryBulkPrice {
    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "price", nullable = false)
    private int price;

    public static CarryDifficultyHistoryBulkPrice from(CarryDifficultyBulkPrice bulkPrice) {
        return new CarryDifficultyHistoryBulkPrice(bulkPrice.getAmount(), bulkPrice.getPrice());
    }

    public static List<CarryDifficultyHistoryBulkPrice> from(List<CarryDifficultyBulkPrice> bulkPrices) {
        return bulkPrices.stream().map(CarryDifficultyHistoryBulkPrice::from).toList();
    }

    public BulkPriceModel toModel() {
        return new BulkPriceModel(amount, price);
    }
}
