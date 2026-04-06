package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity(name = "ticket_panel_closed_category")
@Table(name = "ticket_panel_closed_category", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketPanelClosedCategory {
    @EmbeddedId
    private TicketPanelClosedCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketPanelId")
    @JoinColumn(name = "ticket_panel")
    private TicketPanel ticketPanel;

    public Long getClosedCategoryId() {
        return id.getClosedCategoryId();
    }

    public TicketPanelClosedCategory(TicketPanel ticketPanel, Long closedCategoryId) {
        if(ticketPanel == null) {
            throw new IllegalArgumentException("ticketPanel must not be null");
        }

        this.ticketPanel = ticketPanel;
        this.id = new TicketPanelClosedCategoryId(ticketPanel.getId(), closedCategoryId);
    }
}