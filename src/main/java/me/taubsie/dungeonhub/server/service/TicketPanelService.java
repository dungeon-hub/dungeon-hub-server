package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.TicketPanel;
import me.taubsie.dungeonhub.server.model.TicketPanelInitializeModel;
import me.taubsie.dungeonhub.server.repositories.TicketPanelRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.ticket_panel.TicketPanelCreationModel;
import net.dungeonhub.model.ticket_panel.TicketPanelModel;
import net.dungeonhub.model.ticket_panel.TicketPanelUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class TicketPanelService implements EntityService<TicketPanel, TicketPanelModel, TicketPanelCreationModel, TicketPanelInitializeModel, TicketPanelUpdateModel> {
    private final TicketPanelRepository ticketPanelRepository;

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
    public @NonNull TicketPanel updateEntity(@NonNull TicketPanel ticketPanel, @NonNull TicketPanelUpdateModel ticketPanelUpdateModel) {
        // TODO implement

        return ticketPanel;
    }
}