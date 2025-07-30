package me.taubsie.dungeonhub.server.entities;

import net.dungeonhub.model.reputation.ReputationSumModel;
import org.jetbrains.annotations.NotNull;

public record ReputationSum(@NotNull DiscordUser discordUser, long totalReputation) {
    public ReputationSumModel toReputationSumModel() {
        return new ReputationSumModel(discordUser.toModel(), totalReputation);
    }
}