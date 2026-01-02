package me.taubsie.dungeonhub.server.controller;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordChannel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import me.taubsie.dungeonhub.server.model.TicketPanelInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordChannelService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.TicketPanelService;
import net.dungeonhub.model.ticket_panel.TicketPanelCreationModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import net.dungeonhub.model.ticket_panel.TicketPanelUpdateModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/ticket-panel")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@AllArgsConstructor
public class TicketPanelController {
    private final TicketPanelService ticketPanelService;
    private final DiscordServerService discordServerService;
    private final DiscordChannelService discordChannelService;

    @GetMapping("{id}")
    public TicketPanelModel getById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return ticketPanelService.loadEntityById(discordServer, id)
                .map(TicketPanel::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("all")
    public List<TicketPanelModel> getAllTicketPanels(@PathVariable("server") long serverId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return ticketPanelService.loadEntitiesByDiscordServer(discordServer).stream()
                .map(TicketPanel::toModel)
                .toList();
    }

    @DeleteMapping("{id}")
    public TicketPanelModel deleteById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Optional<TicketPanel> ticketPanel = ticketPanelService.loadEntityById(discordServer, id);

        ticketPanel.ifPresent(ticketPanelService::delete);

        return ticketPanel.map(TicketPanel::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public TicketPanelModel createNewTicketPanel(@PathVariable("server") long serverId, @RequestBody TicketPanelCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Optional<DiscordChannel> transcriptChannel = Optional.ofNullable(creationModel.getTranscriptChannel())
                .map(id -> discordChannelService.loadEntityOrCreate(discordServer, id));

        return ticketPanelService.createEntity(new TicketPanelInitializeModel(discordServer, transcriptChannel.orElse(null))
                .fromCreationModel(creationModel)).toModel();
    }

    @PutMapping("{id}")
    public TicketPanelModel updateTicketPanel(@PathVariable("server") long serverId, @PathVariable long id, @RequestBody TicketPanelUpdateModel updateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        TicketPanel ticketPanel = ticketPanelService.loadEntityById(discordServer, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ticketPanelService.update(ticketPanel, updateModel).toModel();
    }
}