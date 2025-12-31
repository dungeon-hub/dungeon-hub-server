package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.StaticMessage;
import me.taubsie.dungeonhub.server.model.StaticMessageInitializeModel;
import me.taubsie.dungeonhub.server.repositories.StaticMessageRepository;
import net.dungeonhub.enums.StaticMessageType;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.static_message.StaticMessageCreationModel;
import net.dungeonhub.model.static_message.StaticMessageModel;
import net.dungeonhub.model.static_message.StaticMessageUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class StaticMessageService implements EntityService<StaticMessage, StaticMessageModel, StaticMessageCreationModel, StaticMessageInitializeModel, StaticMessageUpdateModel> {
    private final StaticMessageRepository staticMessageRepository;

    @Override
    public @NonNull Optional<StaticMessage> loadEntityById(long id) {
        return staticMessageRepository.findById(id);
    }

    public Optional<StaticMessage> loadEntityById(DiscordServer discordServer, long id) {
        return staticMessageRepository.findById(id)
                .filter(staticMessage -> staticMessage.getServer().getId() == discordServer.getId());
    }

    public List<StaticMessage> loadEntitiesByDiscordServer(DiscordServer discordServer) {
        return staticMessageRepository.findAllByServer(discordServer);
    }

    public List<StaticMessage> loadEntitiesByDiscordServerAndChannel(DiscordServer discordServer, long channelId) {
        return staticMessageRepository.findAllByServerAndChannelId(discordServer, channelId);
    }

    public List<StaticMessage> loadEntitiesByDiscordServerAndMessageType(DiscordServer discordServer, StaticMessageType staticMessageType) {
        return staticMessageRepository.findAllByServerAndStaticMessageType(discordServer, staticMessageType);
    }

    @Override
    public @NonNull List<StaticMessage> findAllEntities() {
        return staticMessageRepository.findAll();
    }

    @Override
    public @NonNull StaticMessage createEntity(@NonNull StaticMessageInitializeModel initializeModel) {
        return saveEntity(initializeModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return staticMessageRepository.findById(id).map(entity ->
        {
            staticMessageRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    public void delete(StaticMessage staticMessage) {
        staticMessageRepository.delete(staticMessage);
    }

    @Override
    public @NonNull StaticMessage saveEntity(@NonNull StaticMessage staticMessage) {
        return staticMessageRepository.save(staticMessage);
    }

    @Override
    public @Nullable Function<StaticMessageModel, StaticMessage> toEntity() {
        return staticMessageModel -> staticMessageRepository.findById(staticMessageModel.getId()).orElseThrow(() -> new EntityUnknownException(staticMessageModel.getId()));
    }

    @Override
    public @NonNull Function<StaticMessage, StaticMessageModel> toModel() {
        return StaticMessage::toModel;
    }

    @Override
    public @NonNull StaticMessage updateEntity(@NonNull StaticMessage staticMessage, @NonNull StaticMessageUpdateModel staticMessageUpdateModel) {
        if(staticMessageUpdateModel.getMessageId() != null) {
            staticMessage.setMessageId(staticMessageUpdateModel.getMessageId());
        }

        if(staticMessageUpdateModel.getChannelId() != null) {
            staticMessage.setChannelId(staticMessageUpdateModel.getChannelId());
        }

        if(staticMessageUpdateModel.getObjectIds() != null) {
            staticMessage.setObjectIds(staticMessageUpdateModel.getObjectIds());
        }

        if(staticMessageUpdateModel.getResetEmbedOverride()) {
            staticMessage.setEmbedOverride(null);
        }

        if(staticMessageUpdateModel.getEmbedOverride() != null) {
            staticMessage.setEmbedOverride(staticMessageUpdateModel.getEmbedOverride());
        }

        return staticMessage;
    }
}