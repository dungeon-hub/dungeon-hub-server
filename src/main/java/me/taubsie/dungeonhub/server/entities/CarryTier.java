package me.taubsie.dungeonhub.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.carry_tier.CarryTierModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity(name = "carry_tier")
@Table(name = "carry_tier", schema = "dungeon-hub")
@NoArgsConstructor
public class CarryTier implements net.dungeonhub.structure.entity.Entity<CarryTierModel> {
    @Getter
    @OneToMany(mappedBy = "carryTier")
    @JsonIgnore
    private final Set<CarryDifficulty> carryDifficulties = new LinkedHashSet<>();
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
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;
    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "carry_type", nullable = false)
    //final
    private CarryType carryType;
    @Setter
    @Column(name = "category")
    private Long category;
    @Setter
    @Column(name = "price_channel")
    private Long priceChannel;
    @Setter
    @Column(name = "descriptive_name", length = 75)
    private String descriptiveName;
    @Setter
    @Column(name = "thumbnail_url", length = 200)
    private String thumbnailUrl;
    @Setter
    @Column(name = "price_title", length = 75)
    private String priceTitle;
    @Setter
    @Column(name = "price_description", length = 200)
    private String priceDescription;

    @SuppressWarnings("java:S107")
    public CarryTier(long id, String identifier, String displayName, CarryType carryType, long category,
                     Long priceChannel, String descriptiveName, String thumbnailUrl, String priceTitle,
                     String priceDescription) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryType = carryType;
        this.category = category;
        this.priceChannel = priceChannel;
        this.descriptiveName = descriptiveName;
        this.thumbnailUrl = thumbnailUrl;
        this.priceTitle = priceTitle;
        this.priceDescription = priceDescription;
    }

    public CarryTier(long id, String identifier, String displayName, CarryType carryType) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryType = carryType;
    }

    @SuppressWarnings("java:S107")
    public CarryTier(String identifier, String displayName, CarryType carryType, long category, long priceChannel,
                     String descriptiveName, String thumbnailUrl, String priceTitle, String priceDescription) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryType = carryType;
        this.category = category;
        this.priceChannel = priceChannel;
        this.descriptiveName = descriptiveName;
        this.thumbnailUrl = thumbnailUrl;
        this.priceTitle = priceTitle;
        this.priceDescription = priceDescription;
    }

    public void setDisplayName(@NotNull String displayName) {
        if (!displayName.isBlank()) {
            this.displayName = displayName;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CarryTier carryTier) {
            return id == carryTier.id
                    || (identifier.equalsIgnoreCase(carryTier.identifier) && carryType.equals(carryTier.carryType));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public @NotNull CarryTierModel toModel() {
        return new CarryTierModel(
                id,
                identifier,
                displayName,
                carryType.toModel(),
                category,
                priceChannel,
                descriptiveName,
                thumbnailUrl,
                priceTitle,
                priceDescription
        );
    }
}