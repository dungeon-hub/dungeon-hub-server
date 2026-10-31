package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.IngameCarryType;
import net.dungeonhub.model.carry_difficulty.BulkPriceModel;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity(name = "carry_difficulty")
@Table(name = "carry_difficulty", schema = "dungeon-hub")
@NoArgsConstructor
public class CarryDifficulty implements net.dungeonhub.structure.entity.Entity<CarryDifficultyModel> {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    //final
    private long id;

    @Getter
    @Column(name = "identifier", nullable = false, length = 50)
    //final
    private String identifier;

    @Getter
    @Setter
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "carry_tier", nullable = false)
    //final
    private CarryTier carryTier;

    @Setter
    @Column(name = "thumbnail_url", length = 200)
    private String thumbnailUrl;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "carry_difficulty_bulk_price", schema = "dungeon-hub",
            joinColumns = @JoinColumn(name = "carry_difficulty", nullable = false))
    @OrderBy("amount")
    private List<CarryDifficultyBulkPrice> bulkPrices = new ArrayList<>();

    @Setter
    @Column(name = "price_name", length = 75)
    private String priceName;

    @Setter
    @Getter
    @Column(name = "price", nullable = false)
    private int price;

    @Setter
    @Column(name = "score", nullable = false)
    private int score;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "ingame_carry_type")
    private IngameCarryType ingameCarryType;

    @SuppressWarnings("java:S107")
    public CarryDifficulty(long id, String identifier, String displayName, CarryTier carryTier, String thumbnailUrl,
                           List<CarryDifficultyBulkPrice> bulkPrices, String priceName, int price, int score, IngameCarryType ingameCarryType) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryTier = carryTier;
        this.thumbnailUrl = thumbnailUrl;
        setBulkPrices(bulkPrices);
        this.priceName = priceName;
        this.price = price;
        this.score = score;
        this.ingameCarryType = ingameCarryType;
    }

    public CarryDifficulty(String identifier, String displayName, CarryTier carryTier, String thumbnailUrl,
                           List<CarryDifficultyBulkPrice> bulkPrices, String priceName, int price, int score, IngameCarryType ingameCarryType) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryTier = carryTier;
        this.thumbnailUrl = thumbnailUrl;
        setBulkPrices(bulkPrices);
        this.priceName = priceName;
        this.price = price;
        this.score = score;
        this.ingameCarryType = ingameCarryType;
    }

    public void setBulkPrices(List<CarryDifficultyBulkPrice> bulkPrices) {
        List<CarryDifficultyBulkPrice> newBulkPrices = bulkPrices == null ? List.of() : List.copyOf(bulkPrices);

        this.bulkPrices.clear();
        this.bulkPrices.addAll(newBulkPrices);
    }

    private List<BulkPriceModel> getBulkPriceModels() {
        return bulkPrices.stream()
                .sorted(Comparator.comparingInt(CarryDifficultyBulkPrice::getAmount))
                .map(CarryDifficultyBulkPrice::toModel)
                .toList();
    }

    public long calculateTotalPrice(int amount) {
        return CarryDifficultyModel.Companion.calculateTotalPrice(amount, getBulkPriceModels(), price);
    }

    public CarryType getCarryType() {
        return getCarryTier().getCarryType();
    }

    public int getScore() {
        return Math.max(score, 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CarryDifficulty carryDifficulty) {
            return id == carryDifficulty.id
                    || (identifier.equalsIgnoreCase(carryDifficulty.identifier) && carryTier.equals(carryDifficulty.carryTier));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public @NotNull CarryDifficultyModel toModel() {
        return new CarryDifficultyModel(
                id,
                identifier,
                displayName,
                carryTier.toModel(),
                price,
                getBulkPriceModels(),
                score,
                thumbnailUrl,
                priceName,
                ingameCarryType
        );
    }
}