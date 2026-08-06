package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.RoleRequirement;
import net.dungeonhub.enums.RoleRequirementComparison;
import net.dungeonhub.enums.RoleRequirementType;
import net.dungeonhub.model.role_requirement.RoleRequirementCreationModel;
import net.dungeonhub.model.role_requirement.RoleRequirementModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class RoleRequirementInitializeModel implements InitializeModel<RoleRequirement, RoleRequirementModel, RoleRequirementCreationModel> {
    private final DiscordRole discordRole;
    private RoleRequirementType requirementType;
    private RoleRequirementComparison comparison;
    private int count;
    @Nullable
    private String extraData;

    public RoleRequirementInitializeModel(DiscordRole discordRole) {
        this.discordRole = discordRole;
    }

    @NotNull
    @Override
    public RoleRequirement toEntity() {
        return new RoleRequirement(discordRole, requirementType, comparison, count, extraData);
    }

    @NotNull
    @Override
    public RoleRequirementInitializeModel fromCreationModel(RoleRequirementCreationModel roleRequirementCreationModel) {
        return new RoleRequirementInitializeModel(discordRole, roleRequirementCreationModel.getRequirementType(), roleRequirementCreationModel.getComparison(), roleRequirementCreationModel.getCount(), roleRequirementCreationModel.getExtraData());
    }
}