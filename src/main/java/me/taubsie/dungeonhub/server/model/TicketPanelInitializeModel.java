package me.taubsie.dungeonhub.server.model;

import dev.kord.common.entity.Permissions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.DiscordChannel;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.enums.TicketPermissionType;
import net.dungeonhub.enums.TranscriptTarget;
import net.dungeonhub.model.ticket_panel.TicketPanelCreationModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class TicketPanelInitializeModel implements InitializeModel<TicketPanel, TicketPanelModel, TicketPanelCreationModel> {
    private final DiscordServer discordServer;
    private final DiscordChannel transcriptChannel;
    private final List<DiscordRole> supportRoles;
    private final List<DiscordRole> additionalRoles;

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
    private TranscriptTarget closeTranscriptTarget;
    private TranscriptTarget deleteTranscriptTarget;
    private String userTranscriptDm;

    private List<Long> openCategories;
    private List<Long> closedCategories;

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

    public TicketPanelInitializeModel(
            DiscordServer discordServer,
            DiscordChannel transcriptChannel,
            List<DiscordRole> supportRoles,
            List<DiscordRole> additionalRoles
    ) {
        this.discordServer = discordServer;
        this.transcriptChannel = transcriptChannel;
        this.supportRoles = supportRoles;
        this.additionalRoles = additionalRoles;
    }

    @Override
    public @NonNull TicketPanel toEntity() {
        return new TicketPanel(name, displayName, emoji, discordServer, closeable, closeConfirmation, claimable, openChannelName, claimedChannelName, closedChannelName, transcriptChannel, ticketMessage, requiresLinking, closeTranscriptTarget, deleteTranscriptTarget, userTranscriptDm, supportRoles, additionalRoles, openCategories, closedCategories, supportTeamAllowedPermissions, supportTeamDeniedPermissions, additionalRolesAllowedPermissions, additionalRolesDeniedPermissions, creatorAllowedPermissions, creatorDeniedPermissions, claimerAllowedPermissions, claimerDeniedPermissions, everyoneAllowedPermissions, everyoneDeniedPermissions);
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
                supportRoles,
                additionalRoles,
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
                ticketPanelCreationModel.getCloseTranscriptTarget() != null ? ticketPanelCreationModel.getCloseTranscriptTarget() : TranscriptTarget.User,
                ticketPanelCreationModel.getDeleteTranscriptTarget() != null ? ticketPanelCreationModel.getDeleteTranscriptTarget() : TranscriptTarget.TranscriptChannel,
                ticketPanelCreationModel.getUserTranscriptDm(),
                ticketPanelCreationModel.getOpenCategories(),
                ticketPanelCreationModel.getClosedCategories(),
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