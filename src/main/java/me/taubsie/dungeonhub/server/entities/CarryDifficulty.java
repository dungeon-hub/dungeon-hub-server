package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

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
    @Setter
    @Column(name = "bulk_price")
    private Integer bulkPrice;

    @Getter
    @Setter
    @Column(name = "bulk_amount")
    private Integer bulkAmount;

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

    @SuppressWarnings("java:S107")
    public CarryDifficulty(long id, String identifier, String displayName, CarryTier carryTier, String thumbnailUrl,
                           Integer bulkPrice, Integer bulkAmount, String priceName, int price, int score) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryTier = carryTier;
        this.thumbnailUrl = thumbnailUrl;
        this.bulkPrice = bulkPrice;
        this.bulkAmount = bulkAmount;
        this.priceName = priceName;
        this.price = price;
        this.score = score;
    }

    public CarryDifficulty(String identifier, String displayName, CarryTier carryTier, String thumbnailUrl,
                           Integer bulkPrice, Integer bulkAmount, String priceName, int price, int score) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryTier = carryTier;
        this.thumbnailUrl = thumbnailUrl;
        this.bulkPrice = bulkPrice;
        this.bulkAmount = bulkAmount;
        this.priceName = priceName;
        this.price = price;
        this.score = score;
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
                bulkPrice,
                bulkAmount,
                score,
                thumbnailUrl,
                priceName
        );
    }
}