package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.discord_user.DiscordUserCreationModel;
import me.taubsie.dungeonhub.common.model.discord_user.DiscordUserModel;
import me.taubsie.dungeonhub.common.model.discord_user.DiscordUserUpdateModel;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.model.DiscordUserInitializeModel;
import me.taubsie.dungeonhub.server.repositories.DiscordUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@Getter
@Setter
@AllArgsConstructor
public class DiscordUserService implements EntityService<DiscordUser, DiscordUserModel, DiscordUserCreationModel,
        DiscordUserInitializeModel, DiscordUserUpdateModel> {
    private final DiscordUserRepository discordUserRepository;

    @Override
    public Optional<DiscordUser> loadEntityById(long id) {
        return discordUserRepository.findById(id);
    }

    public DiscordUser loadEntityOrCreate(long id) {
        return loadEntityById(id).orElseGet(() -> createEntity(new DiscordUserInitializeModel(id, null)));
    }

    @Override
    public Optional<DiscordUser> loadEntityByName(String name) {
        return Optional.empty();
    }

    @Override
    public List<DiscordUser> findAllEntities() {
        return discordUserRepository.findAll();
    }

    @Override
    public DiscordUser createEntity(DiscordUserInitializeModel initalizationModel) {
        return discordUserRepository.save(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return discordUserRepository.findById(id).map(entity ->
        {
            discordUserRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public DiscordUser saveEntity(DiscordUser entity) {
        return discordUserRepository.save(entity);
    }

    @Override
    public Function<DiscordUserModel, DiscordUser> toEntity() {
        return discordUserModel -> loadEntityById(discordUserModel.getId())
                .orElseThrow(() -> new EntityUnknownException(discordUserModel.getId()));
    }

    @Override
    public Function<DiscordUser, DiscordUserModel> toModel() {
        return DiscordUser::toModel;
    }
}