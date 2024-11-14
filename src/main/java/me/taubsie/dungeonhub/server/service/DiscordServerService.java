package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordServerInitializeModel;
import me.taubsie.dungeonhub.server.repositories.DiscordServerRepository;
import net.dungeonhub.expections.EntityUnknownException;
import net.dungeonhub.model.discord_server.DiscordServerModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class DiscordServerService implements EntityService<DiscordServer, DiscordServerModel, DiscordServerModel, DiscordServerInitializeModel, DiscordServerModel> {
    private final DiscordServerRepository discordServerRepository;

    @Autowired
    public DiscordServerService(DiscordServerRepository discordServerRepository) {
        this.discordServerRepository = discordServerRepository;
    }

    @Override
    public @NotNull Optional<DiscordServer> loadEntityById(long id) {
        return discordServerRepository.findById(id);
    }

    public DiscordServer getOrCreate(long id) {
        return loadEntityById(id).orElseGet(() -> discordServerRepository.save(new DiscordServer(id)));
    }

    public DiscordServer getOrCreate(DiscordServerModel discordServerModel) {
        return loadEntityById(discordServerModel.getId()).orElseGet(() ->
                createEntity(new DiscordServerInitializeModel(discordServerModel.getId())));
    }

    @Override
    public @NotNull List<DiscordServer> findAllEntities() {
        return discordServerRepository.findAll();
    }

    @Override
    public @NotNull DiscordServer createEntity(DiscordServerInitializeModel initalizationModel) {
        return discordServerRepository.save(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return discordServerRepository.findById(id).map(entity ->
        {
            discordServerRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public @NotNull DiscordServer saveEntity(@NotNull DiscordServer entity) {
        return discordServerRepository.save(entity);
    }

    @Override
    public Function<DiscordServerModel, DiscordServer> toEntity() {
        return serverModel -> discordServerRepository.findById(serverModel.getId()).orElseThrow(() -> new EntityUnknownException(serverModel.getId()));
    }

    @Override
    public @NotNull Function<DiscordServer, DiscordServerModel> toModel() {
        return server -> new DiscordServerModel(server.getId());
    }

    @Override
    public @NotNull DiscordServer updateEntity(@NotNull DiscordServer discordServer, @NotNull DiscordServerModel discordServerModel) {
        return discordServer;
    }
}