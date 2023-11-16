package me.taubsie.dungeonhub.server.service;

import com.google.errorprone.annotations.DoNotCall;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.server.ServerModel;
import me.taubsie.dungeonhub.server.entities.Server;
import me.taubsie.dungeonhub.server.model.ServerInitializeModel;
import me.taubsie.dungeonhub.server.repositories.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class ServerService implements EntityService<Server, ServerModel, ServerModel, ServerInitializeModel,
        ServerModel> {
    private final ServerRepository serverRepository;

    @Autowired
    public ServerService(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    @Override
    public Optional<Server> loadEntityById(long id) {
        return serverRepository.findById(id);
    }

    public Server getOrCreate(long id) {
        return loadEntityById(id).orElseGet(() -> serverRepository.save(new Server(id)));
    }

    public Server getOrCreate(ServerModel serverModel) {
        return loadEntityById(serverModel.getId()).orElseGet(() ->
                createEntity(new ServerInitializeModel(serverModel.getId())));
    }

    @Override
    @DoNotCall
    public Optional<Server> loadEntityByName(String name) {
        return Optional.empty();
    }

    @Override
    public List<Server> findAllEntities() {
        return serverRepository.findAll();
    }

    @Override
    public Server createEntity(ServerInitializeModel initalizationModel) {
        return serverRepository.save(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return serverRepository.findById(id).map(entity ->
        {
            serverRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public Server saveEntity(Server entity) {
        return serverRepository.save(entity);
    }

    @Override
    public Function<ServerModel, Server> toEntity() {
        return serverModel -> serverRepository.findById(serverModel.getId()).orElseThrow(() -> new EntityUnknownException(serverModel.getId()));
    }

    @Override
    public Function<Server, ServerModel> toModel() {
        return server -> new ServerModel(server.getId());
    }
}