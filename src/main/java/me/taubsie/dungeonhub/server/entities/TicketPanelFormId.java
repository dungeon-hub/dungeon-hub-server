package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TicketPanelFormId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "ticket_panel", nullable = false)
    private Long ticketPanelId;

    @Column(name = "ordinal", nullable = false)
    private int ordinal;
}