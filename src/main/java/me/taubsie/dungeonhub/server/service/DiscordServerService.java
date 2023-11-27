package me.taubsie.dungeonhub.server.service;

import com.google.errorprone.annotations.DoNotCall;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.server.DiscordServerModel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordServerInitializeModel;
import me.taubsie.dungeonhub.server.repositories.DiscordServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class DiscordServerService implements EntityService<DiscordServer, DiscordServerModel, DiscordServerModel, DiscordServerInitializeModel,
        DiscordServerModel> {
    private final DiscordServerRepository discordServerRepository;

    @Autowired
    public DiscordServerService(DiscordServerRepository discordServerRepository) {
        this.discordServerRepository = discordServerRepository;
    }

    @Override
    public Optional<DiscordServer> loadEntityById(long id) {
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
    @DoNotCall
    public Optional<DiscordServer> loadEntityByName(String name) {
        return Optional.empty();
    }

    @Override
    public List<DiscordServer> findAllEntities() {
        return discordServerRepository.findAll();
    }

    @Override
    public DiscordServer createEntity(DiscordServerInitializeModel initalizationModel) {
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
    public DiscordServer saveEntity(DiscordServer entity) {
        return discordServerRepository.save(entity);
    }

    @Override
    public Function<DiscordServerModel, DiscordServer> toEntity() {
        return serverModel -> discordServerRepository.findById(serverModel.getId()).orElseThrow(() -> new EntityUnknownException(serverModel.getId()));
    }

    @Override
    public Function<DiscordServer, DiscordServerModel> toModel() {
        return server -> new DiscordServerModel(server.getId());
    }
}