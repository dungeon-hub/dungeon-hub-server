package me.taubsie.dungeonhub.server.model;

import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.CarryRoleRequirementCreationModel;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.RequirementType;
import me.taubsie.dungeonhub.server.entities.CarryRole;
import me.taubsie.dungeonhub.server.entities.CarryRoleRequirement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record CarryRoleRequirementInitializeModel(@NotNull CarryRole carryRole,
                                                  @NotNull RequirementType requirementType, @NotNull String textValue,
                                                  long requirementValue) implements InitializeModel<CarryRoleRequirement, CarryRoleRequirementCreationModel> {
    @Contract(pure = true)
    @Override
    public @NotNull CarryRoleRequirement toEntity() {
        CarryRoleRequirement carryRoleRequirement = new CarryRoleRequirement();
        carryRoleRequirement.setRole(carryRole());
        carryRoleRequirement.setType(requirementType());
        carryRoleRequirement.setValue(requirementValue());
        return carryRoleRequirement;
    }

    @Contract(pure = true)
    @Override
    public @NotNull InitializeModel<CarryRoleRequirement, CarryRoleRequirementCreationModel> fromCreationModel(@NotNull CarryRoleRequirementCreationModel creationModel) {
        return new CarryRoleRequirementInitializeModel(carryRole(), creationModel.type(), creationModel.textValue(), creationModel.value());
    }
}
