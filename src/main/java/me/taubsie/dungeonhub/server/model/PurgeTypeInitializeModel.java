package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.purge_type.PurgeTypeCreationModel;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.PurgeType;

@AllArgsConstructor
public class PurgeTypeInitializeModel implements InitializeModel<PurgeType, PurgeTypeCreationModel> {
    private final CarryType carryType;
    private String identifier;
    private String displayName;

    public PurgeTypeInitializeModel(CarryType carryType) {
        this.carryType = carryType;
    }

    @Override
    public PurgeType toEntity() {
        return new PurgeType(identifier, displayName, carryType);
    }

    @Override
    public PurgeTypeInitializeModel fromCreationModel(PurgeTypeCreationModel creationModel) {
        return new PurgeTypeInitializeModel(carryType, creationModel.getIdentifier(), creationModel.getDisplayName());
    }
}