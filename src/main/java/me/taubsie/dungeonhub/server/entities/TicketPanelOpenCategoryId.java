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
public class TicketPanelOpenCategoryId implements Serializable {
    @Column(name = "ticket_panel")
    private Long ticketPanelId;

    @Column(name = "open_category")
    private Long openCategoryId;
}