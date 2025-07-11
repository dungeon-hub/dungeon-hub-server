package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Reputation;
import net.dungeonhub.model.reputation.ReputationCreationModel;
import net.dungeonhub.model.reputation.ReputationModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@AllArgsConstructor
public class ReputationInitializeModel implements InitializeModel<Reputation, ReputationModel, ReputationCreationModel> {
    private final DiscordServer discordServer;
    private final DiscordUser user;
    private final DiscordUser reputor;
    private int amount;
    private String reason;

    public ReputationInitializeModel(DiscordServer discordServer, DiscordUser user, DiscordUser reputor) {
        this.discordServer = discordServer;
        this.user = user;
        this.reputor = reputor;
    }

    @NotNull
    @Override
    public Reputation toEntity() {
        return new Reputation(discordServer, user, reputor, amount, reason, Instant.now());
    }

    @NotNull
    @Override
    public ReputationInitializeModel fromCreationModel(ReputationCreationModel reputationCreationModel) {
        return new ReputationInitializeModel(discordServer, user, reputor, reputationCreationModel.getAmount(), reputationCreationModel.getReason());
    }
}