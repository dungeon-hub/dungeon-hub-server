package me.taubsie.dungeonhub.server.service;

import dev.kord.common.entity.Permissions;
import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import me.taubsie.dungeonhub.server.model.TicketPanelInitializeModel;
import me.taubsie.dungeonhub.server.repositories.TicketPanelRepository;
import net.dungeonhub.enums.TicketPermissionCandidate;
import net.dungeonhub.enums.TicketPermissionType;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.ticket_panel.TicketPanelCreationModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import net.dungeonhub.model.ticket_panel.TicketPanelUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class TicketPanelService implements EntityService<TicketPanel, TicketPanelModel, TicketPanelCreationModel, TicketPanelInitializeModel, TicketPanelUpdateModel> {
    private final TicketPanelRepository ticketPanelRepository;
    private final DiscordChannelService discordChannelService;
    private final DiscordRoleService discordRoleService;

    @Override
    public @NonNull Optional<TicketPanel> loadEntityById(long id) {
        return ticketPanelRepository.findById(id);
    }

    public Optional<TicketPanel> loadEntityById(DiscordServer discordServer, long id) {
        return ticketPanelRepository.findById(id)
                .filter(ticketPanel -> ticketPanel.getDiscordServer().getId() == discordServer.getId());
    }

    @Override
    public @NonNull List<TicketPanel> findAllEntities() {
        return ticketPanelRepository.findAll();
    }

    public List<TicketPanel> loadEntitiesByDiscordServer(DiscordServer discordServer) {
        return ticketPanelRepository.findTicketPanelsByDiscordServer(discordServer);
    }

    @Override
    public @NonNull TicketPanel createEntity(@NonNull TicketPanelInitializeModel initializeModel) {
        return saveEntity(initializeModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return ticketPanelRepository.findById(id).map(entity ->
        {
            ticketPanelRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    public void delete(TicketPanel ticketPanel) {
        ticketPanelRepository.delete(ticketPanel);
    }

    @Override
    public @NonNull TicketPanel saveEntity(@NonNull TicketPanel ticketPanel) {
        return ticketPanelRepository.save(ticketPanel);
    }

    @Override
    public @NonNull Function<TicketPanelModel, TicketPanel> toEntity() {
        return ticketPanelModel -> ticketPanelRepository.findById(ticketPanelModel.getId()).orElseThrow(() -> new EntityUnknownException(ticketPanelModel.getId()));
    }

    @Override
    public @NonNull Function<TicketPanel, TicketPanelModel> toModel() {
        return TicketPanel::toModel;
    }

    @Override
    public @NonNull TicketPanel updateEntity(@NonNull TicketPanel ticketPanel, @NonNull TicketPanelUpdateModel updateModel) {
        if(updateModel.getName() != null) {
            ticketPanel.setName(updateModel.getName());
        }

        if(updateModel.getResetDisplayName()) {
            ticketPanel.setDisplayName(null);
        }

        if(updateModel.getDisplayName() != null) {
            ticketPanel.setDisplayName(updateModel.getDisplayName());
        }

        if(updateModel.getResetEmoji()) {
            ticketPanel.setEmoji(null);
        }

        if(updateModel.getEmoji() != null) {
            ticketPanel.setEmoji(updateModel.getEmoji());
        }

        if(updateModel.getCloseable() != null) {
            ticketPanel.setCloseable(updateModel.getCloseable());
        }

        if(updateModel.getCloseConfirmation() != null) {
            ticketPanel.setCloseConfirmation(updateModel.getCloseConfirmation());
        }

        if(updateModel.getClaimable() != null) {
            ticketPanel.setClaimable(updateModel.getClaimable());
        }

        if(updateModel.getResetOpenChannelName()) {
            ticketPanel.setOpenChannelName(null);
        }

        if(updateModel.getOpenChannelName() != null) {
            ticketPanel.setOpenChannelName(updateModel.getOpenChannelName());
        }

        if(updateModel.getResetClaimedChannelName()) {
            ticketPanel.setClaimedChannelName(null);
        }

        if(updateModel.getClaimedChannelName() != null) {
            ticketPanel.setClaimedChannelName(updateModel.getClaimedChannelName());
        }

        if(updateModel.getResetClosedChannelName()) {
            ticketPanel.setClosedChannelName(null);
        }

        if(updateModel.getClosedChannelName() != null) {
            ticketPanel.setClosedChannelName(updateModel.getClosedChannelName());
        }

        if(updateModel.getResetTranscriptChannel()) {
            ticketPanel.setTranscriptChannel(null);
        }

        if(updateModel.getTranscriptChannel() != null) {
            ticketPanel.setTranscriptChannel(
                    discordChannelService.loadEntityOrCreate(
                            ticketPanel.getDiscordServer(),
                            updateModel.getTranscriptChannel()
                    )
            );
        }

        if(updateModel.getResetTicketMessage()) {
            ticketPanel.setTicketMessage(null);
        }

        if(updateModel.getTicketMessage() != null) {
            ticketPanel.setTicketMessage(updateModel.getTicketMessage());
        }

        if(updateModel.getRequiresLinking() != null) {
            ticketPanel.setRequiresLinking(updateModel.getRequiresLinking());
        }

        if(updateModel.getCloseTranscriptTarget() != null) {
            ticketPanel.setCloseTranscriptTarget(updateModel.getCloseTranscriptTarget());
        }

        if(updateModel.getDeleteTranscriptTarget() != null) {
            ticketPanel.setDeleteTranscriptTarget(updateModel.getDeleteTranscriptTarget());
        }

        if(updateModel.getResetUserTranscriptDm()) {
            ticketPanel.setUserTranscriptDm(null);
        }

        if(updateModel.getUserTranscriptDm() != null) {
            ticketPanel.setUserTranscriptDm(updateModel.getUserTranscriptDm());
        }

        if(updateModel.getFormQuestions() != null) {
            ticketPanel.setFormQuestions(updateModel.getFormQuestions());
        }

        if(updateModel.getSupportRoles() != null) {
            ticketPanel.setSupportRoles(
                    updateModel.getSupportRoles().stream()
                            .map(id -> discordRoleService.loadOrCreate(ticketPanel.getDiscordServer(), id))
                            .toList()
            );
        }

        if(updateModel.getAdditionalRoles() != null) {
            ticketPanel.setAdditionalRoles(
                    updateModel.getAdditionalRoles().stream()
                            .map(id -> discordRoleService.loadOrCreate(ticketPanel.getDiscordServer(), id))
                            .toList()
            );
        }

        if(updateModel.getOpenCategories() != null) {
            ticketPanel.setOpenCategories(updateModel.getOpenCategories());
        }

        if(updateModel.getClosedCategories() != null) {
            ticketPanel.setClosedCategories(updateModel.getClosedCategories());
        }

        if(updateModel.getPermissions() != null) {
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.SupportTeam, TicketPermissionType.Allowed, ticketPanel::setSupportTeamAllowedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.SupportTeam, TicketPermissionType.Denied, ticketPanel::setSupportTeamDeniedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.AdditionalRoles, TicketPermissionType.Allowed, ticketPanel::setAdditionalRolesAllowedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.AdditionalRoles, TicketPermissionType.Denied, ticketPanel::setAdditionalRolesDeniedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.TicketCreator, TicketPermissionType.Allowed, ticketPanel::setCreatorAllowedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.TicketCreator, TicketPermissionType.Denied, ticketPanel::setCreatorDeniedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.TicketClaimer, TicketPermissionType.Allowed, ticketPanel::setClaimerAllowedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.TicketClaimer, TicketPermissionType.Denied, ticketPanel::setClaimerDeniedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.Everyone, TicketPermissionType.Allowed, ticketPanel::setEveryoneAllowedPermissions);
            applyPermissionIfPresent(updateModel.getPermissions(), TicketPermissionCandidate.Everyone, TicketPermissionType.Denied, ticketPanel::setEveryoneDeniedPermissions);
        }

        return ticketPanel;
    }

    private void applyPermissionIfPresent(Map<TicketPermissionCandidate, Map<TicketPermissionType, Permissions>> permissions,
                                          TicketPermissionCandidate candidate,
                                          TicketPermissionType type,
                                          Consumer<Permissions> setter) {
        Permissions permission = permissions.getOrDefault(candidate, Collections.emptyMap()).get(type);
        if(permission != null) {
            setter.accept(permission);
        }
    }
}
