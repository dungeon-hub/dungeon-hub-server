package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscordRoleRepository extends JpaRepository<DiscordRole, Long> {
    List<DiscordRole> findDiscordRolesByDiscordServer(DiscordServer discordServer);
}