package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;
import net.dungeonhub.enums.DiscordChannelPermission;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.enums.TicketPhase;

import java.io.Serializable;

@Setter
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TicketPanelChannelPermissionId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "ticket_panel", nullable = false)
    private Long ticketPanelId;

    @Enumerated
    @Column(name = "phase", nullable = false)
    private TicketPhase phase;

    @Enumerated
    @Column(name = "candidate", nullable = false)
    private TicketPermissionCandidate candidate;

    @Enumerated
    @Column(name = "channel_permission", nullable = false)
    private DiscordChannelPermission permission;
}
