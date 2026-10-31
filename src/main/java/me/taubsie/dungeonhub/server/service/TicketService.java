package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.changes.ChangeAudience;
import me.taubsie.dungeonhub.server.changes.ChangePublisher;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.Ticket;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import me.taubsie.dungeonhub.server.model.TicketInitializeModel;
import me.taubsie.dungeonhub.server.repositories.TicketRepository;
import net.dungeonhub.enums.ChangeAction;
import net.dungeonhub.enums.ChangeEventType;
import net.dungeonhub.enums.TicketState;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.exceptions.InvalidUpdateException;
import net.dungeonhub.model.stats.GlobalTicketStatsModel;
import net.dungeonhub.model.ticket.TicketCreationModel;
import net.dungeonhub.model.ticket.TicketModel;
import net.dungeonhub.model.ticket.TicketUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class TicketService implements EntityService<Ticket, TicketModel, TicketCreationModel, TicketInitializeModel, TicketUpdateModel> {
    private final TicketRepository ticketRepository;
    private final DiscordChannelService discordChannelService;
    private final DiscordUserService discordUserService;
    private final ChangePublisher changePublisher;

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

    public List<Ticket> loadEntitiesByServer(DiscordServer discordServer) {
        return ticketRepository.findTicketsByTicketPanel_DiscordServer(discordServer);
    }

    public List<Ticket> findAllOpenTicketsClaimedByUser(Long userId) {
        return ticketRepository.findTicketsByClaimer_IdAndState(userId, TicketState.Open);
    }

    public List<Ticket> loadEntitiesByServerAndChannel(DiscordServer discordServer, Long channelId) {
        if(channelId == null) {
            return loadEntitiesByServer(discordServer);
        }

        return ticketRepository.findTicketsByTicketPanel_DiscordServerAndDiscordChannel_Id(discordServer, channelId);
    }

    public GlobalTicketStatsModel getGlobalTicketStats() {
        return new GlobalTicketStatsModel(
                ticketRepository.count(),
                ticketRepository.countTicketsByCreatedGreaterThanEqual(Instant.now().minus(60, ChronoUnit.DAYS)),
                ticketRepository.countTicketsByCreatedGreaterThanEqual(Instant.now().minus(30, ChronoUnit.DAYS)),
                ticketRepository.countTicketsByCreatedGreaterThanEqual(Instant.now().minus(14, ChronoUnit.DAYS)),
                ticketRepository.countTicketsByCreatedGreaterThanEqual(Instant.now().minus(7, ChronoUnit.DAYS))
        );
    }

    public long countAllTickets(DiscordServer discordServer) {
        return ticketRepository.countTicketsByTicketPanel_DiscordServer(discordServer);
    }

    @Override
    public @NonNull Ticket createEntity(@NonNull TicketInitializeModel ticketInitializeModel) {
        return saveEntity(ticketInitializeModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return ticketRepository.findById(id).map(entity ->
        {
            delete(entity);
            return true;
        }).orElse(false);
    }

    public void delete(Ticket ticket) {
        TicketModel model = ticket.toModel();
        ticketRepository.delete(ticket);
        publishChange(ChangeAction.Deleted, model, null);
    }

    @Override
    public @NonNull Ticket saveEntity(@NonNull Ticket ticket) {
        return saveEntity(ticket, null);
    }

    /**
     * Overridden to also inform the previous claimer of the ticket, so they know that the ticket isn't claimed by them anymore.
     */
    @Override
    public @NonNull Ticket update(@NonNull Ticket ticket, @NonNull TicketUpdateModel ticketUpdateModel) {
        Long previousClaimerId = ticket.getClaimer() != null ? ticket.getClaimer().getId() : null;

        // the same exception handling as in EntityService#update
        try {
            return saveEntity(updateEntity(ticket, ticketUpdateModel), previousClaimerId);
        } catch (NumberFormatException exception) {
            throw new InvalidUpdateException("Invalid numeric value in update model", exception);
        } catch (UnsupportedOperationException exception) {
            throw new InvalidUpdateException("Unsupported operation in update model", exception);
        }
    }

    private Ticket saveEntity(Ticket ticket, @Nullable Long previousClaimerId) {
        boolean created = ticket.getId() == 0;
        Ticket savedTicket = ticketRepository.save(ticket);
        publishChange(created ? ChangeAction.Created : ChangeAction.Updated, savedTicket.toModel(), previousClaimerId);
        return savedTicket;
    }

    private void publishChange(ChangeAction action, TicketModel ticket, @Nullable Long previousClaimerId) {
        long serverId = ticket.getTicketPanel().getDiscordServer().getId();
        Long claimerId = ticket.getClaimer() != null ? ticket.getClaimer().getId() : null;

        changePublisher.publish(ChangeEventType.Ticket, action, serverId, ticket,
                ChangeAudience.server(serverId).withUsers(Arrays.asList(claimerId, previousClaimerId)));
    }

    @Override
    public @NonNull Function<TicketModel, Ticket> toEntity() {
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