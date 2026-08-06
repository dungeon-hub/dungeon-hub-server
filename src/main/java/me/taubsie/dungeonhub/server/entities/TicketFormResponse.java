package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.ticket.TicketFormResponseModel;

@Entity(name = "ticket_form_response")
@Table(name = "ticket_form_response", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketFormResponse {
    @EmbeddedId
    private TicketFormResponseId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketId")
    @JoinColumn(name = "ticket", nullable = false)
    private Ticket ticket;

    @Setter
    @Column(name = "custom_id", nullable = false)
    private String customId;

    @Setter
    @Column(name = "response_value", nullable = false)
    private String value;

    public int getOrdinal() {
        return id.getOrdinal();
    }

    public TicketFormResponse(Ticket ticket, int ordinal, String customId, String value) {
        this.ticket = ticket;
        this.customId = customId;
        this.value = value;
        this.id = new TicketFormResponseId(ticket.getId(), ordinal);
    }

    public TicketFormResponseModel toModel() {
        return new TicketFormResponseModel(getOrdinal(), customId, value);
    }
}