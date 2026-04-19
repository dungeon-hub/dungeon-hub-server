package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Warning;
import net.dungeonhub.enums.WarningType;
import net.dungeonhub.model.warning.WarningCreationModel;
import net.dungeonhub.model.warning.WarningModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@AllArgsConstructor
public class WarningInitializeModel implements InitializeModel<Warning, WarningModel, WarningCreationModel> {
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
    public @NotNull Warning toEntity() {
        return new Warning(server, user, striker, warningType, reason, active, Instant.now());
    }

    @Override
    public @NotNull WarningInitializeModel fromCreationModel(WarningCreationModel creationModel) {
        return new WarningInitializeModel(server, user, striker, creationModel.getWarningType(), creationModel.getReason(), creationModel.getActive());
    }
}