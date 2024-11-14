package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import net.dungeonhub.model.carry_type.CarryTypeCreationModel;
import net.dungeonhub.model.carry_type.CarryTypeModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class CarryTypeInitializeModel implements InitializeModel<CarryType, CarryTypeModel, CarryTypeCreationModel> {
    private final DiscordServer discordServer;
    private String identifier;
    private String displayName;
    private Long logChannel;
    private Long leaderboardChannel;
    private Boolean eventActive;

    public CarryTypeInitializeModel(DiscordServer discordServer) {
        this.discordServer = discordServer;
    }

    @Override
    public @NotNull CarryType toEntity() {
        return new CarryType(identifier, displayName, discordServer, logChannel, leaderboardChannel, eventActive);
    }

    @Override
    public @NotNull CarryTypeInitializeModel fromCreationModel(CarryTypeCreationModel creationModel) {
        return new CarryTypeInitializeModel(discordServer, creationModel.getIdentifier(),
                creationModel.getDisplayName(), creationModel.getLogChannel(), creationModel.getLeaderboardChannel(),
                creationModel.getEventActive());
    }
}