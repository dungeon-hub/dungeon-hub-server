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
import net.dungeonhub.model.ticket_panel.TicketPanelFormModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
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
    private List<TicketPanelFormModel> formQuestions;

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
        return new TicketPanel(name, displayName, emoji, discordServer, closeable, closeConfirmation, claimable, openChannelName, claimedChannelName, closedChannelName, transcriptChannel, ticketMessage, requiresLinking, closeTranscriptTarget, deleteTranscriptTarget, userTranscriptDm, formQuestions, supportRoles, additionalRoles, openCategories, closedCategories, supportTeamAllowedPermissions, supportTeamDeniedPermissions, additionalRolesAllowedPermissions, additionalRolesDeniedPermissions, creatorAllowedPermissions, creatorDeniedPermissions, claimerAllowedPermissions, claimerDeniedPermissions, everyoneAllowedPermissions, everyoneDeniedPermissions);
    }

    @Override
    public @NonNull TicketPanelInitializeModel fromCreationModel(@NonNull TicketPanelCreationModel ticketPanelCreationModel) {
        Map<TicketPermissionCandidate, Map<TicketPermissionType, Permissions>> permissions = ticketPanelCreationModel.getPermissions() != null ? ticketPanelCreationModel.getPermissions() : Collections.emptyMap();

        Permissions supportTeamAllowedPermissions = getPermission(permissions, TicketPermissionCandidate.SupportTeam, TicketPermissionType.Allowed);
        Permissions supportTeamDeniedPermissions = getPermission(permissions, TicketPermissionCandidate.SupportTeam, TicketPermissionType.Denied);
        Permissions additionalRolesAllowedPermissions = getPermission(permissions, TicketPermissionCandidate.AdditionalRoles, TicketPermissionType.Allowed);
        Permissions additionalRolesDeniedPermissions = getPermission(permissions, TicketPermissionCandidate.AdditionalRoles, TicketPermissionType.Denied);
        Permissions creatorAllowedPermissions = getPermission(permissions, TicketPermissionCandidate.TicketCreator, TicketPermissionType.Allowed);
        Permissions creatorDeniedPermissions = getPermission(permissions, TicketPermissionCandidate.TicketCreator, TicketPermissionType.Denied);
        Permissions claimerAllowedPermissions = getPermission(permissions, TicketPermissionCandidate.TicketClaimer, TicketPermissionType.Allowed);
        Permissions claimerDeniedPermissions = getPermission(permissions, TicketPermissionCandidate.TicketClaimer, TicketPermissionType.Denied);
        Permissions everyoneAllowedPermissions = getPermission(permissions, TicketPermissionCandidate.Everyone, TicketPermissionType.Allowed);
        Permissions everyoneDeniedPermissions = getPermission(permissions, TicketPermissionCandidate.Everyone, TicketPermissionType.Denied);

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
                ticketPanelCreationModel.getFormQuestions() != null ? ticketPanelCreationModel.getFormQuestions() : new ArrayList<>(),
                ticketPanelCreationModel.getOpenCategories() != null ? ticketPanelCreationModel.getOpenCategories() : new ArrayList<>(),
                ticketPanelCreationModel.getClosedCategories() != null ? ticketPanelCreationModel.getClosedCategories() : new ArrayList<>(),
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

    private static Permissions getPermission(Map<TicketPermissionCandidate, Map<TicketPermissionType, Permissions>> permissions,
                                             TicketPermissionCandidate candidate,
                                             TicketPermissionType type) {
        return permissions.getOrDefault(candidate, Collections.emptyMap()).get(type);
    }
}
