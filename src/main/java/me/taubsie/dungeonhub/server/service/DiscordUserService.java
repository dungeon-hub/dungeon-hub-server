package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.model.DiscordUserInitializeModel;
import me.taubsie.dungeonhub.server.repositories.DiscordUserRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.discord_user.DiscordUserCreationModel;
import net.dungeonhub.model.discord_user.DiscordUserModel;
import net.dungeonhub.model.discord_user.DiscordUserUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@Getter
@Setter
@AllArgsConstructor
public class DiscordUserService implements EntityService<DiscordUser, DiscordUserModel, DiscordUserCreationModel, DiscordUserInitializeModel, DiscordUserUpdateModel> {
    private final DiscordUserRepository discordUserRepository;

    @Override
    public @NotNull Optional<DiscordUser> loadEntityById(long id) {
        return discordUserRepository.findById(id);
    }

    public DiscordUser loadEntityOrCreate(long id) {
        return discordUserRepository.loadEntityOrCreate(id);
    }

    public Optional<DiscordUser> loadEntityByMinecraftId(UUID minecraftId) {
        return discordUserRepository.findDiscordUserByMinecraftId(minecraftId);
    }

    @Override
    public @NotNull List<DiscordUser> findAllEntities() {
        return discordUserRepository.findAll();
    }

    @Override
    public @NotNull DiscordUser createEntity(DiscordUserInitializeModel initalizationModel) {
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
    public @NotNull DiscordUser saveEntity(@NotNull DiscordUser entity) {
        return discordUserRepository.save(entity);
    }

    @Override
    public Function<DiscordUserModel, DiscordUser> toEntity() {
        return discordUserModel -> loadEntityById(discordUserModel.getId())
                .orElseThrow(() -> new EntityUnknownException(discordUserModel.getId()));
    }

    @Override
    public @NotNull Function<DiscordUser, DiscordUserModel> toModel() {
        return DiscordUser::toModel;
    }

    public long countLinkedUsers() {
        return discordUserRepository.countDiscordUserByMinecraftIdIsNotNull();
    }

    @Override
    public @NotNull DiscordUser updateEntity(@NotNull DiscordUser discordUser, @NotNull DiscordUserUpdateModel discordUserUpdateModel) {
        if (discordUserUpdateModel.getRemoveMinecraftId()) {
            discordUser.setMinecraftId(null);
        }

        if (discordUserUpdateModel.getMinecraftId() != null) {
            discordUser.setMinecraftId(discordUserUpdateModel.getMinecraftId());
        }

        if(discordUserUpdateModel.getRemovePrimarySkyblockProfile()) {
            discordUser.setPrimarySkyblockProfile(null);
        }

        if(discordUserUpdateModel.getPrimarySkyblockProfile() != null) {
            discordUser.setPrimarySkyblockProfile(discordUserUpdateModel.getPrimarySkyblockProfile());
        }

        return discordUser;
    }
}