package me.taubsie.dungeonhub.server.entities;

import dev.kord.common.entity.Permissions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.server.converter.PermissionsConverter;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.enums.TicketPermissionType;
import net.dungeonhub.enums.TranscriptTarget;
import net.dungeonhub.model.ticket_panel.TicketPanelFormModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Entity(name = "ticket_panel")
@Table(name = "ticket_panel", schema = "dungeon-hub")
@NoArgsConstructor
public class TicketPanel implements net.dungeonhub.structure.entity.Entity<TicketPanelModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id", nullable = false)
    private long id;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "display_name")
    private String displayName;

    @Setter
    @Column(name = "emoji")
    private String emoji;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server", nullable = false)
    private DiscordServer discordServer;

    @Setter
    @Column(name = "closeable", nullable = false)
    private boolean closeable;

    @Setter
    @Column(name = "close_confirmation", nullable = false)
    private boolean closeConfirmation;

    @Setter
    @Column(name = "claimable", nullable = false)
    private boolean claimable;

    @Setter
    @Column(name = "open_channel_name")
    private String openChannelName;

    @Setter
    @Column(name = "claimed_channel_name")
    private String claimedChannelName;

    @Setter
    @Column(name = "closed_channel_name")
    private String closedChannelName;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "transcript_channel")
    private DiscordChannel transcriptChannel;

    @Setter
    @Column(name = "ticket_message")
    private String ticketMessage;

    @Setter
    @Column(name = "requires_linking", nullable = false)
    private boolean requiresLinking;

    @Setter
    @Column(name = "close_transcript_target", nullable = false)
    @Enumerated
    @ColumnDefault("0")
    private TranscriptTarget closeTranscriptTarget;

    @Setter
    @Column(name = "delete_transcript_target", nullable = false)
    @Enumerated
    @ColumnDefault("0")
    private TranscriptTarget deleteTranscriptTarget;

    @Setter
    @Column(name = "user_transcript_dm")
    private String userTranscriptDm;

    @Getter
    @OneToMany(mappedBy = "ticketPanel", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private List<TicketPanelForm> formQuestions = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "ticketPanel", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private List<TicketPanelSupportRole> supportRoles = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "ticketPanel", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private List<TicketPanelAdditionalRole> additionalRoles = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "ticketPanel", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private List<TicketPanelOpenCategory> openCategories = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "ticketPanel", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private List<TicketPanelClosedCategory> closedCategories = new ArrayList<>();

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "support_team_allowed_permissions")
    private Permissions supportTeamAllowedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "support_team_denied_permissions")
    private Permissions supportTeamDeniedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "additional_roles_allowed_permissions")
    private Permissions additionalRolesAllowedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "additional_roles_denied_permissions")
    private Permissions additionalRolesDeniedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "creator_allowed_permissions")
    private Permissions creatorAllowedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "creator_denied_permissions")
    private Permissions creatorDeniedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "claimer_allowed_permissions")
    private Permissions claimerAllowedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "claimer_denied_permissions")
    private Permissions claimerDeniedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "everyone_allowed_permissions")
    private Permissions everyoneAllowedPermissions;

    @Setter
    @Convert(converter = PermissionsConverter.class)
    @Column(name = "everyone_denied_permissions")
    private Permissions everyoneDeniedPermissions;

    /**
     * Main constructor used by initialize models to create a fully configured ticket panel.
     * Collection-based arguments are passed through the dedicated setters below so relationship wiring stays consistent.
     */
    public TicketPanel(
            String name,
            String displayName,
            String emoji,
            DiscordServer discordServer,
            boolean closeable,
            boolean closeConfirmation,
            boolean claimable,
            String openChannelName,
            String claimedChannelName,
            String closedChannelName,
            DiscordChannel transcriptChannel,
            String ticketMessage,
            boolean requiresLinking,
            TranscriptTarget closeTranscriptTarget,
            TranscriptTarget deleteTranscriptTarget,
            String userTranscriptDm,
            List<TicketPanelFormModel> formQuestions,
            List<DiscordRole> supportRoles,
            List<DiscordRole> additionalRoles,
            List<Long> openCategories,
            List<Long> closedCategories,
            Permissions supportTeamAllowedPermissions,
            Permissions supportTeamDeniedPermissions,
            Permissions additionalRolesAllowedPermissions,
            Permissions additionalRolesDeniedPermissions,
            Permissions creatorAllowedPermissions,
            Permissions creatorDeniedPermissions,
            Permissions claimerAllowedPermissions,
            Permissions claimerDeniedPermissions,
            Permissions everyoneAllowedPermissions,
            Permissions everyoneDeniedPermissions
    ) {
        this.name = name;
        this.displayName = displayName;
        this.emoji = emoji;
        this.discordServer = discordServer;
        this.closeable = closeable;
        this.closeConfirmation = closeConfirmation;
        this.claimable = claimable;
        this.openChannelName = openChannelName;
        this.claimedChannelName = claimedChannelName;
        this.closedChannelName = closedChannelName;
        this.transcriptChannel = transcriptChannel;
        this.ticketMessage = ticketMessage;
        this.requiresLinking = requiresLinking;
        this.closeTranscriptTarget = closeTranscriptTarget;
        this.deleteTranscriptTarget = deleteTranscriptTarget;
        this.userTranscriptDm = userTranscriptDm;

        this.setFormQuestions(formQuestions);

        this.setSupportRoles(supportRoles);
        this.setAdditionalRoles(additionalRoles);
        this.setOpenCategories(openCategories);
        this.setClosedCategories(closedCategories);

        this.supportTeamAllowedPermissions = supportTeamAllowedPermissions;
        this.supportTeamDeniedPermissions = supportTeamDeniedPermissions;
        this.additionalRolesAllowedPermissions = additionalRolesAllowedPermissions;
        this.additionalRolesDeniedPermissions = additionalRolesDeniedPermissions;
        this.creatorAllowedPermissions = creatorAllowedPermissions;
        this.creatorDeniedPermissions = creatorDeniedPermissions;
        this.claimerAllowedPermissions = claimerAllowedPermissions;
        this.claimerDeniedPermissions = claimerDeniedPermissions;
        this.everyoneAllowedPermissions = everyoneAllowedPermissions;
        this.everyoneDeniedPermissions = everyoneDeniedPermissions;
    }

    public void setFormQuestions(List<TicketPanelFormModel> formQuestions) {
        if(formQuestions == null) return;

        this.formQuestions.clear();
        for(int i = 0; i < formQuestions.size(); i++) {
            TicketPanelFormModel formModel = formQuestions.get(i);
            this.formQuestions.add(i, new TicketPanelForm(this, formModel.getType(), formModel.getData(), i));
        }
    }

    public void setSupportRoles(List<DiscordRole> supportRoles) {
        if(supportRoles == null) return;

        this.supportRoles.clear();
        this.supportRoles.addAll(supportRoles.stream().map(supportRole -> new TicketPanelSupportRole(this, supportRole)).toList());
    }

    public void setAdditionalRoles(List<DiscordRole> additionalRoles) {
        if(additionalRoles == null) return;

        this.additionalRoles.clear();
        this.additionalRoles.addAll(additionalRoles.stream().map(additionalRole -> new TicketPanelAdditionalRole(this, additionalRole)).toList());
    }

    public void setOpenCategories(List<Long> openCategories) {
        if(openCategories == null) return;

        this.openCategories.clear();
        this.openCategories.addAll(openCategories.stream().map(openCategoryId -> new TicketPanelOpenCategory(this, openCategoryId)).toList());
    }

    public void setClosedCategories(List<Long> closedCategories) {
        if(closedCategories == null) return;

        this.closedCategories.clear();
        this.closedCategories.addAll(closedCategories.stream().map(closedCategoryId -> new TicketPanelClosedCategory(this, closedCategoryId)).toList());
    }

    @Override
    public @NotNull TicketPanelModel toModel() {
        Map<TicketPermissionCandidate, Map<TicketPermissionType, Permissions>> permissions = new HashMap<>();
        setPermissions(permissions, TicketPermissionCandidate.SupportTeam, TicketPermissionType.Allowed, supportTeamAllowedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.SupportTeam, TicketPermissionType.Denied, supportTeamDeniedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.AdditionalRoles, TicketPermissionType.Allowed, additionalRolesAllowedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.AdditionalRoles, TicketPermissionType.Denied, additionalRolesDeniedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.TicketCreator, TicketPermissionType.Allowed, creatorAllowedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.TicketCreator, TicketPermissionType.Denied, creatorDeniedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.TicketClaimer, TicketPermissionType.Allowed, claimerAllowedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.TicketClaimer, TicketPermissionType.Denied, claimerDeniedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.Everyone, TicketPermissionType.Allowed, everyoneAllowedPermissions);
        setPermissions(permissions, TicketPermissionCandidate.Everyone, TicketPermissionType.Denied, everyoneDeniedPermissions);

        return new TicketPanelModel(
                id,
                name,
                displayName,
                emoji,
                discordServer.toModel(),
                closeable,
                closeConfirmation,
                claimable,
                openChannelName,
                claimedChannelName,
                closedChannelName,
                transcriptChannel != null ? transcriptChannel.toModel() : null,
                ticketMessage,
                requiresLinking,
                closeTranscriptTarget,
                deleteTranscriptTarget,
                userTranscriptDm,
                formQuestions.stream().sorted(Comparator.comparingInt(TicketPanelForm::getOrdinal)).map(TicketPanelForm::toModel).toList(),
                supportRoles.stream().map(TicketPanelSupportRole::getSupportRole).map(DiscordRole::toModel).toList(),
                additionalRoles.stream().map(TicketPanelAdditionalRole::getAdditionalRole).map(DiscordRole::toModel).toList(),
                openCategories.stream().map(TicketPanelOpenCategory::getOpenCategoryId).toList(),
                closedCategories.stream().map(TicketPanelClosedCategory::getClosedCategoryId).toList(),
                permissions
        );
    }

    private void setPermissions(Map<TicketPermissionCandidate, Map<TicketPermissionType, Permissions>> permissions, TicketPermissionCandidate permissionCandidate, TicketPermissionType permissionType, Permissions permission) {
        if(permission == null) return;

        permissions.computeIfAbsent(permissionCandidate, k -> new HashMap<>()).put(permissionType, permission);
    }
}