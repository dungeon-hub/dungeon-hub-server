package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.PurgeType;
import net.dungeonhub.model.purge_type.PurgeTypeCreationModel;
import net.dungeonhub.model.purge_type.PurgeTypeModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class PurgeTypeInitializeModel implements InitializeModel<PurgeType, PurgeTypeModel, PurgeTypeCreationModel> {
    private final CarryType carryType;
    private String identifier;
    private String displayName;

    public PurgeTypeInitializeModel(CarryType carryType) {
        this.carryType = carryType;
    }

    @Override
    public @NotNull PurgeType toEntity() {
        return new PurgeType(identifier, displayName, carryType);
    }

    @Override
    public @NotNull PurgeTypeInitializeModel fromCreationModel(PurgeTypeCreationModel creationModel) {
        return new PurgeTypeInitializeModel(carryType, creationModel.getIdentifier(), creationModel.getDisplayName());
    }
}