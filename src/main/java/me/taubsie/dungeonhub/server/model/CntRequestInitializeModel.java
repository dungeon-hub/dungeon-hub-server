package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.CntRequest;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import net.dungeonhub.enums.CntRequestType;
import net.dungeonhub.model.cnt_request.CntRequestCreationModel;
import net.dungeonhub.model.cnt_request.CntRequestModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@AllArgsConstructor
public class CntRequestInitializeModel implements InitializeModel<CntRequest, CntRequestModel, CntRequestCreationModel> {
    private long messageId;
    private CntRequestType requestType;
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
    public @NotNull CntRequest toEntity() {
        return new CntRequest(messageId, requestType, discordServer, user, claimer, time, coinValue, description, requirement, false);
    }

    @Override
    public @NotNull CntRequestInitializeModel fromCreationModel(CntRequestCreationModel creationModel) {
        return new CntRequestInitializeModel(creationModel.getMessageId(), creationModel.getRequestType(), discordServer, user, claimer, creationModel.getTime(), creationModel.getCoinValue(), creationModel.getDescription(), creationModel.getRequirement());
    }
}