package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketPanelRepository extends JpaRepository<TicketPanel, Long> {
    List<TicketPanel> findTicketPanelsByDiscordServer(DiscordServer discordServer);
}