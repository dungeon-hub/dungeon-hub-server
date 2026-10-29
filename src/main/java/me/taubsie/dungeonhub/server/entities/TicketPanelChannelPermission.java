package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.DiscordChannelPermission;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.enums.TicketPhase;

/**
 * A Discord channel permission that is allowed or denied for a candidate in a phase. Permissions without a row aren't overwritten.
 */
@Entity(name = "ticket_panel_channel_permission")
@Table(name = "ticket_panel_channel_permission", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketPanelChannelPermission {
    @EmbeddedId
    private TicketPanelChannelPermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketPanelId")
    @JoinColumn(name = "ticket_panel")
    private TicketPanel ticketPanel;

    @Getter
    @Setter
    @Column(name = "allowed", nullable = false)
    private boolean allowed;

    public TicketPanelChannelPermission(TicketPanel ticketPanel, TicketPhase phase, TicketPermissionCandidate candidate, DiscordChannelPermission permission, boolean allowed) {
        this.ticketPanel = ticketPanel;
        this.id = new TicketPanelChannelPermissionId(ticketPanel.getId(), phase, candidate, permission);
        this.allowed = allowed;
    }

    public TicketPhase getPhase() {
        return id.getPhase();
    }

    public TicketPermissionCandidate getCandidate() {
        return id.getCandidate();
    }

    public DiscordChannelPermission getPermission() {
        return id.getPermission();
    }
}
