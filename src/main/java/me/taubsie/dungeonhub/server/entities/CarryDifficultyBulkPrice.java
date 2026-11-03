package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import net.dungeonhub.model.carry_difficulty.BulkPriceModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CarryDifficultyBulkPrice {
    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "price", nullable = false)
    private int price;

    public BulkPriceModel toModel() {
        return new BulkPriceModel(amount, price);
    }

    public static List<CarryDifficultyBulkPrice> fromModels(List<BulkPriceModel> bulkPrices) {
        if (bulkPrices == null) {
            return List.of();
        }

        Set<Integer> amounts = new HashSet<>();

        for (BulkPriceModel bulkPrice : bulkPrices) {
            if (bulkPrice == null || bulkPrice.getAmount() <= 0 || bulkPrice.getPrice() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bulk prices need a positive amount and price.");
            }

            if (!amounts.add(bulkPrice.getAmount())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one bulk price per amount is allowed.");
            }
        }

        return bulkPrices.stream()
                .map(bulkPrice -> new CarryDifficultyBulkPrice(bulkPrice.getAmount(), bulkPrice.getPrice()))
                .toList();
    }
}
