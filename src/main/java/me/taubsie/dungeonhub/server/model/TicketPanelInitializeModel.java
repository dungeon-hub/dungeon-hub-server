package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.server.entities.*;
import net.dungeonhub.enums.TranscriptTarget;
import net.dungeonhub.model.ticket_panel.TicketPanelCreationModel;
import net.dungeonhub.model.ticket_panel.TicketPanelFormModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import net.dungeonhub.model.ticket_panel.TicketPanelPermissionsModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TicketPanelInitializeModel implements InitializeModel<TicketPanel, TicketPanelModel, TicketPanelCreationModel> {
    private final DiscordServer discordServer;
    private final DiscordChannel transcriptChannel;
    private final CarryTier relatedCarryTier;
    private final CarryDifficulty relatedCarryDifficulty;
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

    private TicketPanelPermissionsModel permissions;

    public TicketPanelInitializeModel(
            DiscordServer discordServer,
            DiscordChannel transcriptChannel,
            CarryTier relatedCarryTier,
            CarryDifficulty relatedCarryDifficulty,
            List<DiscordRole> supportRoles,
            List<DiscordRole> additionalRoles
    ) {
        this.discordServer = discordServer;
        this.transcriptChannel = transcriptChannel;
        this.relatedCarryTier = relatedCarryTier;
        this.relatedCarryDifficulty = relatedCarryDifficulty;
        this.supportRoles = supportRoles;
        this.additionalRoles = additionalRoles;
    }

    @Override
    public @NonNull TicketPanel toEntity() {
        return new TicketPanel(name, displayName, emoji, discordServer, closeable, closeConfirmation, claimable, openChannelName, claimedChannelName, closedChannelName, transcriptChannel, ticketMessage, requiresLinking, closeTranscriptTarget, deleteTranscriptTarget, userTranscriptDm, formQuestions, supportRoles, additionalRoles, openCategories, closedCategories, permissions);
    }

    @Override
    public @NonNull TicketPanelInitializeModel fromCreationModel(@NonNull TicketPanelCreationModel ticketPanelCreationModel) {
        TicketPanelPermissionsModel permissions = ticketPanelCreationModel.getPermissions() != null
                ? ticketPanelCreationModel.getPermissions().resolved()
                : TicketPanelPermissionsModel.Companion.getDEFAULT();

        return new TicketPanelInitializeModel(
                discordServer,
                transcriptChannel,
                relatedCarryTier,
                relatedCarryDifficulty,
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
                permissions
        );
    }
}
