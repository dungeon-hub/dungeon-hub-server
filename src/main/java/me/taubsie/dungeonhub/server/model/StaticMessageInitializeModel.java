package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.StaticMessage;
import net.dungeonhub.enums.StaticMessageType;
import net.dungeonhub.model.static_message.StaticMessageCreationModel;
import net.dungeonhub.model.static_message.StaticMessageModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

@AllArgsConstructor
public class StaticMessageInitializeModel implements InitializeModel<StaticMessage, StaticMessageModel, StaticMessageCreationModel> {
    private DiscordServer server;
    private Long channelId;
    private Long messageId;
    private StaticMessageType staticMessageType;
    @NotNull
    private List<Long> objectIds;

    public StaticMessageInitializeModel(DiscordServer server) {
        this.server = server;
    }

    @Override
    public @NonNull StaticMessage toEntity() {
        return new StaticMessage(server, channelId, messageId, staticMessageType, objectIds);
    }

    @Override
    public @NonNull StaticMessageInitializeModel fromCreationModel(StaticMessageCreationModel creationModel) {
        return new StaticMessageInitializeModel(
                server,
                creationModel.getChannelId(),
                creationModel.getMessageId(),
                creationModel.getStaticMessageType(),
                creationModel.getObjectIds()
        );
    }
}