package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import net.dungeonhub.model.carry_tier.CarryTierCreationModel;
import net.dungeonhub.model.carry_tier.CarryTierModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class CarryTierInitializeModel implements InitializeModel<CarryTier, CarryTierModel, CarryTierCreationModel> {
    private final CarryType carryType;
    private final TicketPanel relatedTicketPanel;

    private String identifier;
    private String displayName;
    private Long category;
    private String descriptiveName;
    private String thumbnailUrl;
    private String priceTitle;
    private String priceDescription;

    public CarryTierInitializeModel(CarryType carryType, TicketPanel relatedTicketPanel) {
        this.carryType = carryType;
        this.relatedTicketPanel = relatedTicketPanel;
    }

    @Override
    public @NotNull CarryTier toEntity() {
        return new CarryTier(identifier, displayName, carryType, relatedTicketPanel, category, descriptiveName,
                thumbnailUrl, priceTitle, priceDescription);
    }

    @Override
    public @NotNull CarryTierInitializeModel fromCreationModel(CarryTierCreationModel creationModel) {
        return new CarryTierInitializeModel(carryType, relatedTicketPanel, creationModel.getIdentifier(), creationModel.getDisplayName(),
                creationModel.getCategory(), creationModel.getDescriptiveName(),
                creationModel.getThumbnailUrl(), creationModel.getPriceTitle(), creationModel.getPriceDescription());
    }
}