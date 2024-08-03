package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.common.enums.WarningType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.WarningPunishment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarningPunishmentRepository extends JpaRepository<WarningPunishment, Long> {
    List<WarningPunishment> findAllByServer(DiscordServer server);

    List<WarningPunishment> findAllByServerAndWarningType(DiscordServer server, WarningType warningType);
}