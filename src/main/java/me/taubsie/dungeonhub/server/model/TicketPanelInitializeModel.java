package me.taubsie.dungeonhub.server.model;

import dev.kord.common.entity.Permissions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.DiscordChannel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.enums.TicketPermissionType;
import net.dungeonhub.model.ticket_panel.TicketPanelCreationModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class TicketPanelInitializeModel implements InitializeModel<TicketPanel, TicketPanelModel, TicketPanelCreationModel> {
    private final DiscordServer discordServer;
    private final DiscordChannel transcriptChannel;

    private String name;
    private String displayName;
    private String emoji;
    private boolean closeable;
    private boolean closeConfirmation;
    private boolean claimable;
    private String openChannelName;
    private String claimedChannelName;
    private String closedChannelName;
    private String ticketMessage;
    private boolean requiresLinking;

    /*
    TODO:
    val supportRoles: List<DiscordRoleModel>,
    val additionalRoles: List<DiscordRoleModel>,
    val openCategories: List<DiscordChannelModel>,
    val closedCategories: List<DiscordChannelModel>,
    */

    private Permissions supportTeamAllowedPermissions;
    private Permissions supportTeamDeniedPermissions;
    private Permissions additionalRolesAllowedPermissions;
    private Permissions additionalRolesDeniedPermissions;
    private Permissions creatorAllowedPermissions;
    private Permissions creatorDeniedPermissions;
    private Permissions claimerAllowedPermissions;
    private Permissions claimerDeniedPermissions;
    private Permissions everyoneAllowedPermissions;
    private Permissions everyoneDeniedPermissions;

    public TicketPanelInitializeModel(DiscordServer discordServer, DiscordChannel transcriptChannel) {
        this.discordServer = discordServer;
        this.transcriptChannel = transcriptChannel;
    }

    @Override
    public @NonNull TicketPanel toEntity() {
        return new TicketPanel(name, displayName, emoji, discordServer, closeable, closeConfirmation, claimable, openChannelName, claimedChannelName, closedChannelName, transcriptChannel, ticketMessage, requiresLinking, supportTeamAllowedPermissions, supportTeamDeniedPermissions, additionalRolesAllowedPermissions, additionalRolesDeniedPermissions, creatorAllowedPermissions, creatorDeniedPermissions, claimerAllowedPermissions, claimerDeniedPermissions, everyoneAllowedPermissions, everyoneDeniedPermissions);
    }

    @Override
    public @NonNull TicketPanelInitializeModel fromCreationModel(@NonNull TicketPanelCreationModel ticketPanelCreationModel) {
        Map<TicketPermissionCandidate, Map<TicketPermissionType, Permissions>> permissions = ticketPanelCreationModel.getPermissions() != null ? ticketPanelCreationModel.getPermissions() : Collections.emptyMap();

        Permissions supportTeamAllowedPermissions = permissions.getOrDefault(TicketPermissionCandidate.SupportTeam, Collections.emptyMap()).get(TicketPermissionType.Allowed);
        Permissions supportTeamDeniedPermissions = permissions.getOrDefault(TicketPermissionCandidate.SupportTeam, Collections.emptyMap()).get(TicketPermissionType.Denied);
        Permissions additionalRolesAllowedPermissions = permissions.getOrDefault(TicketPermissionCandidate.AdditionalRoles, Collections.emptyMap()).get(TicketPermissionType.Allowed);
        Permissions additionalRolesDeniedPermissions = permissions.getOrDefault(TicketPermissionCandidate.AdditionalRoles, Collections.emptyMap()).get(TicketPermissionType.Denied);
        Permissions creatorAllowedPermissions = permissions.getOrDefault(TicketPermissionCandidate.TicketCreator, Collections.emptyMap()).get(TicketPermissionType.Allowed);
        Permissions creatorDeniedPermissions = permissions.getOrDefault(TicketPermissionCandidate.TicketCreator, Collections.emptyMap()).get(TicketPermissionType.Denied);
        Permissions claimerAllowedPermissions = permissions.getOrDefault(TicketPermissionCandidate.TicketClaimer, Collections.emptyMap()).get(TicketPermissionType.Allowed);
        Permissions claimerDeniedPermissions = permissions.getOrDefault(TicketPermissionCandidate.TicketClaimer, Collections.emptyMap()).get(TicketPermissionType.Denied);
        Permissions everyoneAllowedPermissions = permissions.getOrDefault(TicketPermissionCandidate.Everyone, Collections.emptyMap()).get(TicketPermissionType.Allowed);
        Permissions everyoneDeniedPermissions = permissions.getOrDefault(TicketPermissionCandidate.Everyone, Collections.emptyMap()).get(TicketPermissionType.Denied);

        return new TicketPanelInitializeModel(
                discordServer,
                transcriptChannel,
                ticketPanelCreationModel.getName(),
                ticketPanelCreationModel.getDisplayName(),
                ticketPanelCreationModel.getEmoji(),
                ticketPanelCreationModel.getCloseable(),
                ticketPanelCreationModel.getCloseConfirmation(),
                ticketPanelCreationModel.getClaimable(),
                ticketPanelCreationModel.getOpenChannelName(),
                ticketPanelCreationModel.getClaimedChannelName(),
                ticketPanelCreationModel.getClosedChannelName(),
                ticketPanelCreationModel.getTicketMessage(),
                ticketPanelCreationModel.getRequiresLinking(),
                supportTeamAllowedPermissions,
                supportTeamDeniedPermissions,
                additionalRolesAllowedPermissions,
                additionalRolesDeniedPermissions,
                creatorAllowedPermissions,
                creatorDeniedPermissions,
                claimerAllowedPermissions,
                claimerDeniedPermissions,
                everyoneAllowedPermissions,
                everyoneDeniedPermissions
        );
    }
}