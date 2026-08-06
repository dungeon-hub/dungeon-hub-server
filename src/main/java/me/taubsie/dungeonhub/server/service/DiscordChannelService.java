package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordChannel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordChannelInitializeModel;
import me.taubsie.dungeonhub.server.repositories.DiscordChannelRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.discord_channel.DiscordChannelCreationModel;
import net.dungeonhub.model.discord_channel.DiscordChannelModel;
import net.dungeonhub.model.discord_channel.DiscordChannelUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class DiscordChannelService implements EntityService<DiscordChannel, DiscordChannelModel, DiscordChannelCreationModel, DiscordChannelInitializeModel, DiscordChannelUpdateModel> {
    private final DiscordChannelRepository discordChannelRepository;

    @Override
    public @NotNull Optional<DiscordChannel> loadEntityById(long id) {
        return discordChannelRepository.findById(id);
    }

    public DiscordChannel loadEntityOrCreate(DiscordServer discordServer, long id) {
        return discordChannelRepository.loadEntityOrCreate(discordServer, id);
    }

    public Optional<DiscordChannel> loadEntityById(DiscordServer discordServer, long id) {
        return discordChannelRepository.findById(id)
                .filter(discordChannel -> discordChannel.getDiscordServer().getId() == discordServer.getId());
    }

    @Override
    public @NotNull List<DiscordChannel> findAllEntities() {
        return discordChannelRepository.findAll();
    }

    public List<DiscordChannel> loadEntitiesByDiscordServer(DiscordServer discordServer) {
        return discordChannelRepository.findDiscordChannelsByDiscordServer(discordServer);
    }

    @Override
    public @NotNull DiscordChannel createEntity(DiscordChannelInitializeModel initializeModel) {
        return discordChannelRepository.save(initializeModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return discordChannelRepository.findById(id).map(entity ->
        {
            discordChannelRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    public void delete(DiscordChannel discordChannel) {
        discordChannelRepository.delete(discordChannel);
    }

    @Override
    public @NotNull DiscordChannel saveEntity(@NotNull DiscordChannel entity) {
        return discordChannelRepository.save(entity);
    }

    @Override
    public Function<DiscordChannelModel, DiscordChannel> toEntity() {
        return discordChannelModel -> loadEntityById(discordChannelModel.getId())
                .orElseThrow(() -> new EntityUnknownException(discordChannelModel.getId()));
    }

    @Override
    public @NotNull Function<DiscordChannel, DiscordChannelModel> toModel() {
        return DiscordChannel::toModel;
    }

    @Override
    public @NotNull DiscordChannel updateEntity(@NotNull DiscordChannel discordChannel, @NotNull DiscordChannelUpdateModel discordChannelUpdateModel) {
        if(discordChannelUpdateModel.getName() != null) {
            discordChannel.setName(discordChannelUpdateModel.getName());
        }

        if(discordChannelUpdateModel.getDeleted() != null) {
            discordChannel.setDeleted(discordChannelUpdateModel.getDeleted());
        }

        return discordChannel;
    }
}