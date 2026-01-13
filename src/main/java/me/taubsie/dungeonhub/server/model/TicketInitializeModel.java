package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.DiscordChannel;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Ticket;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import net.dungeonhub.enums.TicketState;
import net.dungeonhub.model.ticket.TicketCreationModel;
import net.dungeonhub.model.ticket.TicketFormResponseModel;
import net.dungeonhub.model.ticket.TicketModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TicketInitializeModel implements InitializeModel<Ticket, TicketModel, TicketCreationModel> {
    private final DiscordChannel discordChannel;
    private final TicketPanel ticketPanel;
    private final DiscordUser user;
    private final DiscordUser claimer;

    private TicketState state;
    private Instant created;
    private List<TicketFormResponseModel> formResponses;

    public TicketInitializeModel(DiscordChannel discordChannel, TicketPanel ticketPanel, DiscordUser user, DiscordUser claimer) {
        this.discordChannel = discordChannel;
        this.ticketPanel = ticketPanel;
        this.user = user;
        this.claimer = claimer;
    }

    @Override
    public @NonNull Ticket toEntity() {
        return new Ticket(state, discordChannel, ticketPanel, user, claimer, created, formResponses);
    }

    @Override
    public @NonNull TicketInitializeModel fromCreationModel(@NonNull TicketCreationModel ticketCreationModel) {
        return new TicketInitializeModel(
                discordChannel,
                ticketPanel,
                user,
                claimer,
                ticketCreationModel.getState(),
                Instant.now(),
                ticketCreationModel.getFormResponses() != null ? ticketCreationModel.getFormResponses() : Collections.emptyList()
        );
    }
}