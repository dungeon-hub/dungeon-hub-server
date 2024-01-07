package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordRoleGroup;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscordRoleGroupRepository extends JpaRepository<DiscordRoleGroup, Long> {
    List<DiscordRoleGroup> findDiscordRoleGroupsByDiscordRole_DiscordServer(DiscordServer discordServer);
}