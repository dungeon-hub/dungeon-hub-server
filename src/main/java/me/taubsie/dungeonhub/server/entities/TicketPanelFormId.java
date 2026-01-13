package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Setter
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TicketPanelFormId {
    @Column(name = "ticket_panel", nullable = false)
    private Long ticketPanelId;

    @Column(name = "ordinal", nullable = false)
    private int ordinal;
}