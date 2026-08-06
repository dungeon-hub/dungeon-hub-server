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
public class TicketFormResponseId {
    @Column(name = "ticket", nullable = false)
    private Long ticketId;

    @Column(name = "ordinal", nullable = false)
    private int ordinal;
}