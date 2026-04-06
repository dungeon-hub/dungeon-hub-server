package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.Ticket;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findTicketsByTicketPanel(TicketPanel ticketPanel);
    List<Ticket> findTicketsByTicketPanel_DiscordServer(DiscordServer discordServer);
    List<Ticket> findTicketsByTicketPanel_DiscordServerAndDiscordChannel_Id(DiscordServer discordServer, long channelId);
}
