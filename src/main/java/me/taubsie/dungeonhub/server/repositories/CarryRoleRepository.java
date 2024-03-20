package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CarryRole;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CarryRoleRepository extends JpaRepository<CarryRole, Long> {

    @NotNull
    @Unmodifiable
    Set<CarryRole> findAllByDiscordRole(@NotNull DiscordRole discordRole);

    @NotNull
    Optional<CarryRole> findByDisplayName(@NotNull String displayName);

}
