package me.taubsie.dungeonhub.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierModel;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Entity(name = "carry_tier")
@Table(name = "carry_tier", schema = "dungeon-hub")
public class CarryTier implements EntityModelRelation<CarryTierModel> {
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
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
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
                     long priceChannel, String descriptiveName, String thumbnailUrl, String priceTitle,
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

    public CarryTier() {

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

    public static CarryTier fromResultSet(ResultSet resultSet, Map<Long, CarryType> carryTypes) throws SQLException {
        long carryType = resultSet.getLong("carry_type");

        CarryTier carryTier = new CarryTier(
                resultSet.getLong("id"),
                resultSet.getString("identifier"),
                resultSet.getString("display_name"),
                carryTypes.getOrDefault(carryType, null)
        );

        if (resultSet.getLong("price_channel") > 0) {
            carryTier.setPriceChannel(resultSet.getLong("price_channel"));
        }

        if (resultSet.getLong("category") > 0) {
            carryTier.setCategory(resultSet.getLong("category"));
        }

        if (resultSet.getString("descriptive_name") != null) {
            carryTier.setDescriptiveName(resultSet.getString("descriptive_name"));
        }

        if (resultSet.getString("thumbnail_url") != null) {
            carryTier.setThumbnailUrl(resultSet.getString("thumbnail_url"));
        }

        if (resultSet.getString("price_title") != null) {
            carryTier.setPriceTitle(resultSet.getString("price_title"));
        }

        if (resultSet.getString("price_description") != null) {
            carryTier.setPriceDescription(resultSet.getString("price_description"));
        }

        return carryTier;
    }

    public void setDisplayName(@NotNull String displayName) {
        if (!displayName.isBlank()) {
            this.displayName = displayName;
        }
    }

    public String getDescriptiveName() {
        return getActualDescriptiveName().orElse(getDisplayName());
    }

    public Optional<String> getActualDescriptiveName() {
        return Optional.ofNullable(descriptiveName == null || descriptiveName.isBlank() ? null : descriptiveName);
    }

    public Optional<Long> getCategory() {
        return Optional.ofNullable(category > 0L ? category : null);
    }

    public Optional<String> getThumbnailUrl() {
        return Optional.ofNullable(thumbnailUrl == null || thumbnailUrl.isBlank() ? null : thumbnailUrl);
    }

    public String getPriceTitle() {
        return getActualPriceTitle().orElse(getDescriptiveName());
    }

    public Optional<String> getActualPriceTitle() {
        return Optional.ofNullable(priceTitle == null || priceTitle.isBlank() ? null : priceTitle);
    }

    public Optional<String> getPriceDescription() {
        return Optional.ofNullable(priceDescription == null || priceDescription.isBlank() ? null : priceDescription);
    }

    public Optional<Long> getPriceChannel() {
        return Optional.ofNullable(priceChannel > 0L ? priceChannel : null);
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
    public CarryTier fromModel(CarryTierModel model) {
        return new CarryTier(model.getId(), model.getIdentifier(), model.getDisplayName(),
                carryType.fromModel(model.getCarryType()), model.getActualCategory(), model.getActualPriceChannel(),
                model.getActualDescriptiveName().orElse(null), model.getActualThumbnailUrl(),
                model.getActualPriceTitle().orElse(null), model.getActualPriceDescription());
    }

    @Override
    public CarryTierModel toModel() {
        return new CarryTierModel(category, priceChannel, descriptiveName, thumbnailUrl, priceTitle, priceDescription,
                id, identifier, displayName, carryType.toModel());
    }
}