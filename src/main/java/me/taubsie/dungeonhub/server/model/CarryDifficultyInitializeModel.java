package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyCreationModel;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class CarryDifficultyInitializeModel implements InitializeModel<CarryDifficulty, CarryDifficultyModel, CarryDifficultyCreationModel> {
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
    public @NotNull CarryDifficulty toEntity() {
        return new CarryDifficulty(identifier, displayName, carryTier, thumbnailUrl, bulkPrice, bulkAmount, priceName, price,
                score);
    }

    @Override
    public @NotNull CarryDifficultyInitializeModel fromCreationModel(CarryDifficultyCreationModel creationModel) {
        return new CarryDifficultyInitializeModel(creationModel.getIdentifier(), creationModel.getDisplayName(),
                carryTier, creationModel.getThumbnailUrl(),
                creationModel.getBulkPrice(), creationModel.getBulkAmount(), creationModel.getPriceName(),
                creationModel.getPrice(), creationModel.getScore());
    }
}