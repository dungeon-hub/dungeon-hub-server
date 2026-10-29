package me.taubsie.dungeonhub.server.service;

import com.google.gson.Gson;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import me.taubsie.dungeonhub.server.model.TicketPanelInitializeModel;
import me.taubsie.dungeonhub.server.repositories.TicketPanelRepository;
import net.dungeonhub.enums.DiscordChannelPermission;
import net.dungeonhub.enums.TicketAction;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.exceptions.InvalidUpdateException;
import net.dungeonhub.model.ticket_panel.TicketCandidatePermissionsModel;
import net.dungeonhub.model.ticket_panel.TicketPanelCreationModel;
import net.dungeonhub.model.ticket_panel.TicketPanelPermissionsModel;
import net.dungeonhub.model.ticket_panel.TicketPanelUpdateModel;
import net.dungeonhub.service.GsonService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * The request bodies are parsed with the same Gson instance as the HTTP layer uses.
 */
class TicketPanelPermissionsTest {
    private static final TicketPanelPermissionsModel DEFAULT = TicketPanelPermissionsModel.Companion.getDEFAULT();

    @SuppressWarnings("deprecation")
    private final Gson gson = GsonService.INSTANCE.getGson();

    private final TicketPanelService ticketPanelService = new TicketPanelService(
            mock(TicketPanelRepository.class),
            mock(DiscordChannelService.class),
            mock(DiscordRoleService.class),
            mock(CarryTierService.class),
            mock(CarryDifficultyService.class)
    );

    @Test
    void creationWithoutPermissionsUsesDefaults() {
        TicketPanelCreationModel creationModel = gson.fromJson("{\"name\":\"support\"}", TicketPanelCreationModel.class);

        TicketPanel ticketPanel = initializeModel().fromCreationModel(creationModel).toEntity();

        assertEquals(DEFAULT, ticketPanel.getResolvedPermissions());
    }

    @Test
    void creationResolvesGivenPermissions() {
        TicketPanelCreationModel creationModel = gson.fromJson(
                "{\"name\":\"support\",\"permissions\":{\"open\":{\"TicketCreator\":{\"actions\":{\"Close\":false},\"channel\":{\"AttachFiles\":true}}}}}",
                TicketPanelCreationModel.class
        );

        TicketPanelPermissionsModel permissions = initializeModel().fromCreationModel(creationModel).getPermissions();
        TicketCandidatePermissionsModel creator = permissions.getOpen().get(TicketPermissionCandidate.TicketCreator);

        assertEquals(false, creator.getActions().get(TicketAction.Close));
        assertEquals(true, creator.getActions().get(TicketAction.Delete));
        assertEquals(Map.of(DiscordChannelPermission.AttachFiles, true), creator.getChannel());
        assertEquals(DEFAULT.getClosed(), permissions.getClosed());
    }

    @Test
    void creationRejectsInvalidPermissions() {
        TicketPanelCreationModel creationModel = gson.fromJson(
                "{\"name\":\"support\",\"permissions\":{\"open\":{\"SupportTeam\":{\"actions\":{\"Close\":null}}}}}",
                TicketPanelCreationModel.class
        );

        assertThrows(InvalidUpdateException.class, () -> initializeModel().fromCreationModel(creationModel));
    }

    @Test
    void entityWithoutPermissionsUsesDefaults() {
        assertEquals(DEFAULT, new TicketPanel().getResolvedPermissions());
    }

    @Test
    void updateWithoutPermissionsKeepsThem() {
        TicketPanel ticketPanel = new TicketPanel();
        TicketPanelPermissionsModel stored = permissionsWithClosingCreator(false);
        ticketPanel.setPermissions(stored);

        TicketPanelUpdateModel updateModel = gson.fromJson("{\"name\":\"renamed\"}", TicketPanelUpdateModel.class);
        ticketPanelService.updateEntity(ticketPanel, updateModel);

        assertEquals(stored, ticketPanel.getResolvedPermissions());
    }

    @Test
    void updateReplacesPermissions() {
        TicketPanel ticketPanel = new TicketPanel();
        ticketPanel.setPermissions(permissionsWithClosingCreator(false));

        // the replacement doesn't mention the creator, so the creator goes back to the defaults
        TicketPanelUpdateModel updateModel = gson.fromJson(
                "{\"permissions\":{\"closed\":{\"Everyone\":{\"channel\":{}}}}}",
                TicketPanelUpdateModel.class
        );
        ticketPanelService.updateEntity(ticketPanel, updateModel);

        TicketPanelPermissionsModel permissions = ticketPanel.getResolvedPermissions();

        assertEquals(DEFAULT.getOpen(), permissions.getOpen());
        assertEquals(Map.of(), permissions.getClosed().get(TicketPermissionCandidate.Everyone).getChannel());
    }

    @Test
    void updateRejectsInvalidPermissions() {
        TicketPanel ticketPanel = new TicketPanel();

        TicketPanelUpdateModel updateModel = gson.fromJson(
                "{\"permissions\":{\"open\":{\"Moderators\":{}}}}",
                TicketPanelUpdateModel.class
        );

        assertThrows(InvalidUpdateException.class, () -> ticketPanelService.updateEntity(ticketPanel, updateModel));
    }

    private static TicketPanelInitializeModel initializeModel() {
        return new TicketPanelInitializeModel(null, null, null, null, List.of(), List.of());
    }

    private static TicketPanelPermissionsModel permissionsWithClosingCreator(boolean allowed) {
        return new TicketPanelPermissionsModel(
                Map.of(TicketPermissionCandidate.TicketCreator, new TicketCandidatePermissionsModel(
                        Map.of(TicketAction.Close, allowed),
                        Map.of()
                )),
                Map.of()
        ).resolved();
    }
}
