package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.enums.WarningType;
import me.taubsie.dungeonhub.common.model.warning.WarningCreationModel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Warning;

import java.time.Instant;

@AllArgsConstructor
public class WarningInitializeModel implements InitializeModel<Warning, WarningCreationModel> {
    private final DiscordServer server;
    private final DiscordUser user;
    private final DiscordUser striker;
    private WarningType warningType;
    private String reason;
    private boolean active;

    public WarningInitializeModel(DiscordServer server, DiscordUser user, DiscordUser striker) {
        this.server = server;
        this.user = user;
        this.striker = striker;
    }

    @Override
    public Warning toEntity() {
        return new Warning(server, user, striker, warningType, reason, active, Instant.now());
    }

    @Override
    public WarningInitializeModel fromCreationModel(WarningCreationModel creationModel) {
        return new WarningInitializeModel(server, user, striker, creationModel.getWarningType(), creationModel.getReason(), creationModel.isActive());
    }
}