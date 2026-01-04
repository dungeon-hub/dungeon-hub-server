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
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    public @Nullable Function<TicketPanelModel, TicketPanel> toEntity() {
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
            Permissions supportTeamAllowedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.SupportTeam, Collections.emptyMap()).get(TicketPermissionType.Allowed);
            if(supportTeamAllowedPermissions != null) {
                ticketPanel.setSupportTeamAllowedPermissions(supportTeamAllowedPermissions);
            }
            Permissions supportTeamDeniedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.SupportTeam, Collections.emptyMap()).get(TicketPermissionType.Denied);
            if(supportTeamDeniedPermissions != null) {
                ticketPanel.setSupportTeamDeniedPermissions(supportTeamDeniedPermissions);
            }
            Permissions additionalRolesAllowedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.AdditionalRoles, Collections.emptyMap()).get(TicketPermissionType.Allowed);
            if(additionalRolesAllowedPermissions != null) {
                ticketPanel.setAdditionalRolesAllowedPermissions(additionalRolesAllowedPermissions);
            }
            Permissions additionalRolesDeniedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.AdditionalRoles, Collections.emptyMap()).get(TicketPermissionType.Denied);
            if(additionalRolesDeniedPermissions != null) {
                ticketPanel.setAdditionalRolesDeniedPermissions(additionalRolesDeniedPermissions);
            }
            Permissions creatorAllowedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.TicketCreator, Collections.emptyMap()).get(TicketPermissionType.Allowed);
            if(creatorAllowedPermissions != null) {
                ticketPanel.setCreatorAllowedPermissions(creatorAllowedPermissions);
            }
            Permissions creatorDeniedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.TicketCreator, Collections.emptyMap()).get(TicketPermissionType.Denied);
            if(creatorDeniedPermissions != null) {
                ticketPanel.setCreatorDeniedPermissions(creatorDeniedPermissions);
            }
            Permissions claimerAllowedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.TicketClaimer, Collections.emptyMap()).get(TicketPermissionType.Allowed);
            if(claimerAllowedPermissions != null) {
                ticketPanel.setClaimerAllowedPermissions(claimerAllowedPermissions);
            }
            Permissions claimerDeniedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.TicketClaimer, Collections.emptyMap()).get(TicketPermissionType.Denied);
            if(claimerDeniedPermissions != null) {
                ticketPanel.setClaimerDeniedPermissions(claimerDeniedPermissions);
            }
            Permissions everyoneAllowedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.Everyone, Collections.emptyMap()).get(TicketPermissionType.Allowed);
            if(everyoneAllowedPermissions != null) {
                ticketPanel.setEveryoneAllowedPermissions(everyoneAllowedPermissions);
            }
            Permissions everyoneDeniedPermissions = updateModel.getPermissions().getOrDefault(TicketPermissionCandidate.Everyone, Collections.emptyMap()).get(TicketPermissionType.Denied);
            if(everyoneDeniedPermissions != null) {
                ticketPanel.setEveryoneDeniedPermissions(everyoneDeniedPermissions);
            }
        }

        return ticketPanel;
    }
}