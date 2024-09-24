package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.cnt_request.CntRequestCreationModel;
import me.taubsie.dungeonhub.server.entities.CntRequest;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;

import java.time.Instant;

@AllArgsConstructor
public class CntRequestInitializeModel implements InitializeModel<CntRequest, CntRequestCreationModel> {
    private long messageId;
    private final DiscordServer discordServer;
    private final DiscordUser user;
    private final DiscordUser claimer;
    private Instant time;
    private String coinValue;
    private String description;
    private String requirement;

    public CntRequestInitializeModel(DiscordServer discordServer, DiscordUser user, DiscordUser claimer) {
        this.discordServer = discordServer;
        this.user = user;
        this.claimer = claimer;
    }

    @Override
    public CntRequest toEntity() {
        return new CntRequest(messageId, discordServer, user, claimer, time, coinValue, description, requirement);
    }

    @Override
    public CntRequestInitializeModel fromCreationModel(CntRequestCreationModel creationModel) {
        return new CntRequestInitializeModel(creationModel.getMessageId(), discordServer, user, claimer, creationModel.getTime(), creationModel.getCoinValue(), creationModel.getDescription(), creationModel.getRequirement());
    }
}