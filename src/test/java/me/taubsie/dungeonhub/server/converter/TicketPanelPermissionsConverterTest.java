package me.taubsie.dungeonhub.server.converter;

import net.dungeonhub.enums.DiscordChannelPermission;
import net.dungeonhub.enums.TicketAction;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.model.ticket_panel.TicketCandidatePermissionsModel;
import net.dungeonhub.model.ticket_panel.TicketPanelPermissionsModel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketPanelPermissionsConverterTest {
    private final TicketPanelPermissionsConverter converter = new TicketPanelPermissionsConverter();

    @Test
    void storesAndLoadsPermissions() {
        TicketPanelPermissionsModel permissions = new TicketPanelPermissionsModel(
                Map.of(TicketPermissionCandidate.TicketCreator, new TicketCandidatePermissionsModel(
                        Map.of(TicketAction.Close, false),
                        Map.of(DiscordChannelPermission.AttachFiles, true)
                )),
                Map.of()
        ).resolved();

        String json = converter.convertToDatabaseColumn(permissions);

        assertTrue(json.contains("\"TicketCreator\""));
        assertEquals(permissions, converter.convertToEntityAttribute(json));
    }

    @Test
    void storesAndLoadsDefaults() {
        TicketPanelPermissionsModel defaults = TicketPanelPermissionsModel.Companion.getDEFAULT();

        assertEquals(defaults, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(defaults)));
    }

    @Test
    void keepsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }
}
