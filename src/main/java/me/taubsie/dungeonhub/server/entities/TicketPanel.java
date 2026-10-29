package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.server.converter.TicketPanelPermissionsConverter;
import net.dungeonhub.enums.TranscriptTarget;
import net.dungeonhub.model.ticket_panel.TicketPanelFormModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import net.dungeonhub.model.ticket_panel.TicketPanelPermissionsModel;
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
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "related_carry_tier")
    private CarryTier relatedCarryTier;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "related_carry_difficulty")
    private CarryDifficulty relatedCarryDifficulty;

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

    /**
     * The resolved permissions of this panel. {@code null} means that the defaults are used.
     */
    @Setter
    @Convert(converter = TicketPanelPermissionsConverter.class)
    @Column(name = "permissions")
    private TicketPanelPermissionsModel permissions;

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
            TicketPanelPermissionsModel permissions
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

        this.permissions = permissions;
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
                relatedCarryTier != null ? relatedCarryTier.toModel() : null,
                relatedCarryDifficulty != null ? relatedCarryDifficulty.toModel() : null,
                supportRoles.stream().map(TicketPanelSupportRole::getSupportRole).map(DiscordRole::toModel).toList(),
                additionalRoles.stream().map(TicketPanelAdditionalRole::getAdditionalRole).map(DiscordRole::toModel).toList(),
                openCategories.stream().map(TicketPanelOpenCategory::getOpenCategoryId).toList(),
                closedCategories.stream().map(TicketPanelClosedCategory::getClosedCategoryId).toList(),
                getResolvedPermissions()
        );
    }

    public TicketPanelPermissionsModel getResolvedPermissions() {
        return permissions != null ? permissions.resolved() : TicketPanelPermissionsModel.Companion.getDEFAULT();
    }
}