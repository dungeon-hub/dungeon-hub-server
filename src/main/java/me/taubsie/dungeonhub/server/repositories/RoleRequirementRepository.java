package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.RoleRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRequirementRepository extends JpaRepository<RoleRequirement, Long> {
    List<RoleRequirement> findRoleRequirementsByDiscordRole_DiscordServer(DiscordServer discordServer);
}