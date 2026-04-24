package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "ticket_panel_support_role")
@Table(name = "ticket_panel_support_role", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketPanelSupportRole {
    @EmbeddedId
    private TicketPanelSupportRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketPanelId")
    @JoinColumn(name = "ticket_panel")
    private TicketPanel ticketPanel;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("supportRoleId")
    @JoinColumn(name = "support_role")
    private DiscordRole supportRole;

    public TicketPanelSupportRole(TicketPanel ticketPanel, DiscordRole supportRole) {
        this.ticketPanel = ticketPanel;
        this.supportRole = supportRole;
        this.id = new TicketPanelSupportRoleId(ticketPanel.getId(), supportRole.getId());
    }
}