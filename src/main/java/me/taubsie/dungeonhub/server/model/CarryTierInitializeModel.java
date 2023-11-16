package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierCreationModel;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;

@AllArgsConstructor
@Getter
@Setter
public class CarryTierInitializeModel implements InitializeModel<CarryTier, CarryTierCreationModel> {
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
    public CarryTier toEntity() {
        return new CarryTier(identifier, displayName, carryType, category, priceChannel, descriptiveName,
                thumbnailUrl, priceTitle, priceDescription);
    }

    @Override
    public CarryTierInitializeModel fromCreationModel(CarryTierCreationModel creationModel) {
        return new CarryTierInitializeModel(creationModel.getIdentifier(), creationModel.getDisplayName(), carryType,
                creationModel.getCategory(), creationModel.getPriceChannel(), creationModel.getDescriptiveName(),
                creationModel.getThumbnailUrl(), creationModel.getPriceTitle(), creationModel.getPriceDescription());
    }
}