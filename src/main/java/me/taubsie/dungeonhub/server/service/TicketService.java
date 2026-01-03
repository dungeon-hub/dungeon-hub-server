package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.Ticket;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import me.taubsie.dungeonhub.server.model.TicketInitializeModel;
import me.taubsie.dungeonhub.server.repositories.TicketRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.ticket.TicketCreationModel;
import net.dungeonhub.model.ticket.TicketModel;
import net.dungeonhub.model.ticket.TicketUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class TicketService implements EntityService<Ticket, TicketModel, TicketCreationModel, TicketInitializeModel, TicketUpdateModel> {
    private final TicketRepository ticketRepository;
    private final DiscordChannelService discordChannelService;
    private final DiscordUserService discordUserService;

    @Override
    public @NonNull Optional<Ticket> loadEntityById(long id) {
        return ticketRepository.findById(id);
    }

    public Optional<Ticket> loadEntityById(TicketPanel ticketPanel, long id) {
        return ticketRepository.findById(id)
                .filter(ticket -> ticket.getTicketPanel().getId() == ticketPanel.getId());
    }

    @Override
    public @NonNull List<Ticket> findAllEntities() {
        return ticketRepository.findAll();
    }

    public List<Ticket> loadEntitiesByTicketPanel(TicketPanel ticketPanel) {
        return ticketRepository.findTicketsByTicketPanel(ticketPanel);
    }

    @Override
    public @NonNull Ticket createEntity(@NonNull TicketInitializeModel ticketInitializeModel) {
        return saveEntity(ticketInitializeModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return ticketRepository.findById(id).map(entity ->
        {
            ticketRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    public void delete(Ticket ticket) {
        ticketRepository.delete(ticket);
    }

    @Override
    public @NonNull Ticket saveEntity(@NonNull Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    public @Nullable Function<TicketModel, Ticket> toEntity() {
        return ticketModel -> ticketRepository.findById(ticketModel.getId()).orElseThrow(() -> new EntityUnknownException(ticketModel.getId()));
    }

    @Override
    public @NonNull Function<Ticket, TicketModel> toModel() {
        return Ticket::toModel;
    }

    @Override
    public @NonNull Ticket updateEntity(@NonNull Ticket ticket, @NonNull TicketUpdateModel ticketUpdateModel) {
        if(ticketUpdateModel.getState() != null) {
            ticket.setState(ticketUpdateModel.getState());
        }

        if(ticketUpdateModel.getChannel() != null) {
            ticket.setDiscordChannel(discordChannelService.loadEntityOrCreate(ticket.getTicketPanel().getDiscordServer(), ticketUpdateModel.getChannel()));
        }

        if(ticketUpdateModel.getResetClaimer()) {
            ticket.setClaimer(null);
        }

        if(ticketUpdateModel.getClaimer() != null) {
            ticket.setClaimer(discordUserService.loadEntityOrCreate(ticketUpdateModel.getClaimer()));
        }

        return ticket;
    }
}