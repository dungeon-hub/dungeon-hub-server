package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.FormType;
import net.dungeonhub.model.ticket_panel.TicketPanelFormModel;
import org.hibernate.annotations.ColumnDefault;

@Entity(name = "ticket_panel_form")
@Table(name = "ticket_panel_form", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketPanelForm {
    @EmbeddedId
    private TicketPanelFormId id;

    @Setter
    @Column(name = "form_type", nullable = false)
    @Enumerated
    @ColumnDefault("0")
    private FormType formType;

    @Setter
    @Column(name = "data")
    private String data;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketPanelId")
    @JoinColumn(name = "ticket_panel")
    private TicketPanel ticketPanel;

    public int getOrdinal() {
        return id.getOrdinal();
    }

    public TicketPanelForm(TicketPanel ticketPanel, FormType formType, String data, int ordinal) {
        this.ticketPanel = ticketPanel;
        this.formType = formType;
        this.data = data;
        this.id = new TicketPanelFormId(ticketPanel.getId(), ordinal);
    }

    public TicketPanelFormModel toModel() {
        return new TicketPanelFormModel(formType, data);
    }
}