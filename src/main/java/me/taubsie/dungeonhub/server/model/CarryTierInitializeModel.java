package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import net.dungeonhub.model.carry_tier.CarryTierCreationModel;
import net.dungeonhub.model.carry_tier.CarryTierModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class CarryTierInitializeModel implements InitializeModel<CarryTier, CarryTierModel, CarryTierCreationModel> {
    private String identifier;
    private String displayName;
    private CarryType carryType;
    private Long category;
    private Long priceChannel;
    private String descriptiveName;
    private String thumbnailUrl;
    private String priceTitle;
    private String priceDescription;

    public CarryTierInitializeModel(CarryType carryType) {
        this.carryType = carryType;
    }

    @Override
    public @NotNull CarryTier toEntity() {
        return new CarryTier(identifier, displayName, carryType, category, priceChannel, descriptiveName,
                thumbnailUrl, priceTitle, priceDescription);
    }

    @Override
    public @NotNull CarryTierInitializeModel fromCreationModel(CarryTierCreationModel creationModel) {
        return new CarryTierInitializeModel(creationModel.getIdentifier(), creationModel.getDisplayName(), carryType,
                creationModel.getCategory(), creationModel.getPriceChannel(), creationModel.getDescriptiveName(),
                creationModel.getThumbnailUrl(), creationModel.getPriceTitle(), creationModel.getPriceDescription());
    }
}