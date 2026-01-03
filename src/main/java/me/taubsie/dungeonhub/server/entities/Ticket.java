package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.TicketState;
import net.dungeonhub.model.ticket.TicketModel;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jspecify.annotations.NonNull;

import java.time.Instant;

@Entity(name = "ticket")
@Table(name = "ticket", schema = "dungeon-hub")
@NoArgsConstructor
public class Ticket implements net.dungeonhub.structure.entity.Entity<TicketModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id", nullable = false)
    private long id;

    @Setter
    @Column(name = "state", nullable = false)
    @Enumerated
    @ColumnDefault("0")
    private TicketState state;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "channel")
    private DiscordChannel discordChannel;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "ticket_panel", nullable = false)
    private TicketPanel ticketPanel;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private DiscordUser user;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "claimer")
    private DiscordUser claimer;

    @Column(name = "created", nullable = false)
    private Instant created;

    public Ticket(TicketState state, DiscordChannel discordChannel, TicketPanel ticketPanel, DiscordUser user, DiscordUser claimer, Instant created) {
        this.state = state;
        this.discordChannel = discordChannel;
        this.ticketPanel = ticketPanel;
        this.user = user;
        this.claimer = claimer;
        this.created = created;
    }

    @Override
    public @NonNull TicketModel toModel() {
        return new TicketModel(
                id,
                state,
                discordChannel != null ? discordChannel.toModel() : null,
                ticketPanel.toModel(),
                user.toModel(),
                claimer != null ? claimer.toModel() : null,
                created
        );
    }
}