package me.taubsie.dungeonhub.server.model;

import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.carryrole.CarryRoleCreationModel;
import me.taubsie.dungeonhub.server.entities.CarryRole;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record CarryRoleInitializeModel(@NotNull DiscordRole discordRole, @NotNull String displayName, boolean manual, boolean enabled) implements InitializeModel<CarryRole, CarryRoleCreationModel> {
    @Contract(pure = true)
    @Override
    public @NotNull CarryRole toEntity() {
        CarryRole carryRole = new CarryRole();

        //TODO maybe add requirements

        carryRole.setDiscordRole(discordRole());
        carryRole.setDisplayName(displayName());
        carryRole.setManual(manual());
        carryRole.setEnabled(enabled());
        return carryRole;
    }

    @Contract(pure = true)
    @Override
    public @NotNull InitializeModel<CarryRole, CarryRoleCreationModel> fromCreationModel(@NotNull CarryRoleCreationModel creationModel) {
        return new CarryRoleInitializeModel(discordRole(), creationModel.displayName(), creationModel.manual(), creationModel.enable());
    }
}
