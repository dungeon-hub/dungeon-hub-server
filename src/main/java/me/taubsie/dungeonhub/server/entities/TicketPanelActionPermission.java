package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.TicketAction;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.enums.TicketPhase;

/**
 * Whether a candidate may perform a ticket action in a phase. Every action of a phase has a row once the permissions are stored.
 */
@Entity(name = "ticket_panel_action_permission")
@Table(name = "ticket_panel_action_permission", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketPanelActionPermission {
    @EmbeddedId
    private TicketPanelActionPermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketPanelId")
    @JoinColumn(name = "ticket_panel")
    private TicketPanel ticketPanel;

    @Getter
    @Setter
    @Column(name = "allowed", nullable = false)
    private boolean allowed;

    public TicketPanelActionPermission(TicketPanel ticketPanel, TicketPhase phase, TicketPermissionCandidate candidate, TicketAction action, boolean allowed) {
        this.ticketPanel = ticketPanel;
        this.id = new TicketPanelActionPermissionId(ticketPanel.getId(), phase, candidate, action);
        this.allowed = allowed;
    }

    public TicketPhase getPhase() {
        return id.getPhase();
    }

    public TicketPermissionCandidate getCandidate() {
        return id.getCandidate();
    }

    public TicketAction getAction() {
        return id.getAction();
    }
}
