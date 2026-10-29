package me.taubsie.dungeonhub.server.entities;

import net.dungeonhub.enums.DiscordChannelPermission;
import net.dungeonhub.enums.TicketAction;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.enums.TicketPhase;
import net.dungeonhub.model.ticket_panel.TicketCandidatePermissionsModel;
import net.dungeonhub.model.ticket_panel.TicketPanelPermissionsModel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class TicketPanelPermissionsMappingTest {
    private static final TicketPanelPermissionsModel DEFAULT = TicketPanelPermissionsModel.Companion.getDEFAULT();

    @Test
    void storesEveryActionAndTheGivenChannelPermissions() {
        TicketPanel ticketPanel = new TicketPanel();
        ticketPanel.setPermissions(DEFAULT);

        int expectedActions = 0;
        int expectedChannelPermissions = 0;
        for(TicketPhase phase : TicketPhase.values()) {
            for(TicketCandidatePermissionsModel candidatePermissions : DEFAULT.forPhase(phase).values()) {
                expectedActions += candidatePermissions.getActions().size();
                expectedChannelPermissions += candidatePermissions.getChannel().size();
            }
        }

        // 5 candidates with 6 actions while open and 3 actions while closed
        assertEquals(45, expectedActions);
        assertEquals(expectedActions, ticketPanel.getActionPermissions().size());
        assertEquals(expectedChannelPermissions, ticketPanel.getChannelPermissions().size());
        assertEquals(DEFAULT, ticketPanel.getPermissions());
    }

    @Test
    void roundTripsCustomPermissions() {
        TicketPanelPermissionsModel permissions = new TicketPanelPermissionsModel(
                Map.of(TicketPermissionCandidate.TicketCreator, new TicketCandidatePermissionsModel(
                        Map.of(TicketAction.Close, false),
                        Map.of(DiscordChannelPermission.AttachFiles, true, DiscordChannelPermission.SendMessages, false)
                )),
                Map.of(TicketPermissionCandidate.Everyone, new TicketCandidatePermissionsModel(Map.of(), Map.of()))
        ).resolved();

        TicketPanel ticketPanel = new TicketPanel();
        ticketPanel.setPermissions(permissions);

        assertEquals(permissions, ticketPanel.getPermissions());
    }

    @Test
    void updatesExistingRowsInPlace() {
        TicketPanel ticketPanel = new TicketPanel();
        ticketPanel.setPermissions(DEFAULT);

        TicketPanelActionPermission creatorClose = findAction(ticketPanel, TicketPhase.Open, TicketPermissionCandidate.TicketCreator, TicketAction.Close);
        int rows = ticketPanel.getActionPermissions().size();

        ticketPanel.setPermissions(new TicketPanelPermissionsModel(
                Map.of(TicketPermissionCandidate.TicketCreator, new TicketCandidatePermissionsModel(Map.of(TicketAction.Close, false), Map.of())),
                Map.of()
        ));

        assertSame(creatorClose, findAction(ticketPanel, TicketPhase.Open, TicketPermissionCandidate.TicketCreator, TicketAction.Close));
        assertFalse(creatorClose.isAllowed());
        assertEquals(rows, ticketPanel.getActionPermissions().size());
    }

    @Test
    void removesChannelPermissionsThatAreNoLongerGiven() {
        TicketPanel ticketPanel = new TicketPanel();
        ticketPanel.setPermissions(DEFAULT);

        int rows = ticketPanel.getChannelPermissions().size();

        ticketPanel.setPermissions(new TicketPanelPermissionsModel(
                Map.of(TicketPermissionCandidate.Everyone, new TicketCandidatePermissionsModel(Map.of(), Map.of())),
                Map.of()
        ));

        assertEquals(rows - 1, ticketPanel.getChannelPermissions().size());
        assertEquals(Map.of(), ticketPanel.getPermissions().getOpen().get(TicketPermissionCandidate.Everyone).getChannel());
    }

    @Test
    void missingRowsFallBackToTheDefaults() {
        TicketPanel ticketPanel = new TicketPanel();
        ticketPanel.setPermissions(new TicketPanelPermissionsModel(
                Map.of(TicketPermissionCandidate.TicketCreator, new TicketCandidatePermissionsModel(Map.of(TicketAction.Close, false), Map.of())),
                Map.of()
        ));

        // e.g. rows of an action or a candidate that was added after the permissions were stored
        ticketPanel.getActionPermissions().removeIf(row -> row.getCandidate() == TicketPermissionCandidate.TicketClaimer
                || (row.getCandidate() == TicketPermissionCandidate.TicketCreator && row.getAction() == TicketAction.Delete));
        ticketPanel.getChannelPermissions().removeIf(row -> row.getCandidate() == TicketPermissionCandidate.TicketClaimer);

        TicketPanelPermissionsModel permissions = ticketPanel.getPermissions();

        assertEquals(DEFAULT.getOpen().get(TicketPermissionCandidate.TicketClaimer), permissions.getOpen().get(TicketPermissionCandidate.TicketClaimer));
        assertEquals(true, permissions.getOpen().get(TicketPermissionCandidate.TicketCreator).getActions().get(TicketAction.Delete));
        assertEquals(false, permissions.getOpen().get(TicketPermissionCandidate.TicketCreator).getActions().get(TicketAction.Close));
    }

    private static TicketPanelActionPermission findAction(TicketPanel ticketPanel, TicketPhase phase, TicketPermissionCandidate candidate, TicketAction action) {
        return ticketPanel.getActionPermissions().stream()
                .filter(row -> row.getPhase() == phase && row.getCandidate() == candidate && row.getAction() == action)
                .findFirst()
                .orElseThrow();
    }
}
