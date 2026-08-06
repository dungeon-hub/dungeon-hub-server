package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.model.DiscordUserInitializeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscordUserRepository extends JpaRepository<DiscordUser, Long> {
    long countDiscordUserByMinecraftIdIsNotNull();

    Optional<DiscordUser> findDiscordUserByMinecraftId(UUID minecraftId);

    default DiscordUser loadEntityOrCreate(long id) {
        return findById(id).orElseGet(() -> save(new DiscordUserInitializeModel(id, null, null).toEntity()));
    }
}