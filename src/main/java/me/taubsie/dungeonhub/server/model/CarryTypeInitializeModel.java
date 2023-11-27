package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.common.model.carry_type.CarryTypeCreationModel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;

@AllArgsConstructor
public class CarryTypeInitializeModel implements InitializeModel<CarryType, CarryTypeCreationModel> {
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
    public CarryType toEntity() {
        return new CarryType(identifier, displayName, discordServer, logChannel, leaderboardChannel, eventActive);
    }

    @Override
    public CarryTypeInitializeModel fromCreationModel(CarryTypeCreationModel creationModel) {
        return new CarryTypeInitializeModel(discordServer, creationModel.getIdentifier(),
                creationModel.getDisplayName(), creationModel.getLogChannel(), creationModel.getLeaderboardChannel(),
                creationModel.getEventActive());
    }
}