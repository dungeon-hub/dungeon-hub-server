package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyCreationModel;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryTier;

@AllArgsConstructor
@Getter
@Setter
public class CarryDifficultyInitializeModel implements InitializeModel<CarryDifficulty, CarryDifficultyCreationModel> {
    private String identifier;
    private String displayName;
    private CarryTier carryTier;
    private String thumbnailUrl;
    private Integer bulkPrice;
    private Integer bulkAmount;
    private String priceName;
    private Integer price;
    private Integer score;

    public CarryDifficultyInitializeModel(CarryTier carryTier) {
        this.carryTier = carryTier;
    }

    @Override
    public CarryDifficulty toEntity() {
        return new CarryDifficulty(identifier, displayName, thumbnailUrl, bulkPrice, bulkAmount, priceName, price,
                score);
    }

    @Override
    public CarryDifficultyInitializeModel fromCreationModel(CarryDifficultyCreationModel creationModel) {
        return new CarryDifficultyInitializeModel(creationModel.getIdentifier(), creationModel.getDisplayName(),
                carryTier, creationModel.getThumbnailUrl(),
                creationModel.getBulkPrice(), creationModel.getBulkAmount(), creationModel.getPriceName(),
                creationModel.getPrice(), creationModel.getScore());
    }
}