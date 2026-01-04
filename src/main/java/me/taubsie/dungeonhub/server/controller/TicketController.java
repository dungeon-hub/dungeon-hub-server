package me.taubsie.dungeonhub.server.controller;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.model.TicketInitializeModel;
import me.taubsie.dungeonhub.server.service.*;
import net.dungeonhub.model.ticket.TicketCreationModel;
import net.dungeonhub.model.ticket.TicketModel;
import net.dungeonhub.model.ticket.TicketUpdateModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/ticket-panel/{ticket-panel}/ticket")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@AllArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final TicketPanelService ticketPanelService;
    private final DiscordServerService discordServerService;
    private final DiscordChannelService discordChannelService;
    private final DiscordUserService discordUserService;

    @GetMapping("{id}")
    public TicketModel getById(@PathVariable("server") long serverId, @PathVariable("ticket-panel") long ticketPanelId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        TicketPanel ticketPanel = ticketPanelService.loadEntityById(discordServer, ticketPanelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ticketService.loadEntityById(ticketPanel, id)
                .map(Ticket::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("all")
    public List<TicketModel> getAllTickets(@PathVariable("server") long serverId, @PathVariable("ticket-panel") long ticketPanelId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        TicketPanel ticketPanel = ticketPanelService.loadEntityById(discordServer, ticketPanelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ticketService.loadEntitiesByTicketPanel(ticketPanel).stream()
                .map(Ticket::toModel)
                .toList();
    }

    @PostMapping
    public TicketModel createNewTicket(@PathVariable("server") long serverId, @PathVariable("ticket-panel") long ticketPanelId, @RequestBody TicketCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        TicketPanel ticketPanel = ticketPanelService.loadEntityById(discordServer, ticketPanelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Optional<DiscordChannel> discordChannel = Optional.ofNullable(creationModel.getChannel()).map(channelId -> discordChannelService.loadEntityOrCreate(discordServer, channelId));

        DiscordUser user = discordUserService.loadEntityOrCreate(creationModel.getUser());

        Optional<DiscordUser> claimer = Optional.ofNullable(creationModel.getClaimer()).map(discordUserService::loadEntityOrCreate);

        return ticketService.createEntity(
                new TicketInitializeModel(
                        discordChannel.orElse(null),
                        ticketPanel,
                        user,
                        claimer.orElse(null)
                ).fromCreationModel(creationModel)
        ).toModel();
    }

    @PutMapping("{id}")
    public TicketModel updateTicket(@PathVariable("server") long serverId, @PathVariable("ticket-panel") long ticketPanelId, @PathVariable long id, @RequestBody TicketUpdateModel updateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        TicketPanel ticketPanel = ticketPanelService.loadEntityById(discordServer, ticketPanelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Ticket ticket = ticketService.loadEntityById(ticketPanel, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ticketService.update(ticket, updateModel).toModel();
    }
}