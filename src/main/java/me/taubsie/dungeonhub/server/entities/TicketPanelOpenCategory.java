package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity(name = "ticket_panel_open_category")
@Table(name = "ticket_panel_open_category", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketPanelOpenCategory {
    @EmbeddedId
    private TicketPanelOpenCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketPanelId")
    @JoinColumn(name = "ticket_panel")
    private TicketPanel ticketPanel;

    public Long getOpenCategoryId() {
        return id.getOpenCategoryId();
    }

    public TicketPanelOpenCategory(TicketPanel ticketPanel, Long openCategoryId) {
        this.ticketPanel = ticketPanel;
        this.id = new TicketPanelOpenCategoryId(ticketPanel.getId(), openCategoryId);
    }
}