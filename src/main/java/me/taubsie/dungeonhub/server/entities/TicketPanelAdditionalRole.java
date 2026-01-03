package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "ticket_panel_additional_role")
@Table(name = "ticket_panel_additional_role", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketPanelAdditionalRole {
    @EmbeddedId
    private TicketPanelAdditionalRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketPanelId")
    @JoinColumn(name = "ticket_panel")
    private TicketPanel ticketPanel;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("additionalRoleId")
    @JoinColumn(name = "additional_role")
    private DiscordRole additionalRole;

    public TicketPanelAdditionalRole(TicketPanel ticketPanel, DiscordRole additionalRole) {
        this.ticketPanel = ticketPanel;
        this.additionalRole = additionalRole;
        this.id = new TicketPanelAdditionalRoleId(ticketPanel.getId(), additionalRole.getId());
    }
}