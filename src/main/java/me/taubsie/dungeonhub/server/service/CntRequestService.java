package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.CntRequest;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.model.CntRequestInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CntRequestRepository;
import me.taubsie.dungeonhub.server.repositories.DiscordUserRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.cnt_request.CntRequestCreationModel;
import net.dungeonhub.model.cnt_request.CntRequestModel;
import net.dungeonhub.model.cnt_request.CntRequestUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class CntRequestService implements EntityService<CntRequest, CntRequestModel, CntRequestCreationModel, CntRequestInitializeModel, CntRequestUpdateModel> {

    private final CntRequestRepository cntRequestRepository;
    private final DiscordUserRepository discordUserRepository;

    @Override
    public @NotNull Optional<CntRequest> loadEntityById(long id) {
        return cntRequestRepository.findById(id);
    }

    public Optional<CntRequest> loadEntityById(DiscordServer discordServer, long id) {
        return cntRequestRepository.findById(id)
                .filter(cntRequest -> cntRequest.getDiscordServer().getId() == discordServer.getId());
    }

    @Override
    public @NotNull List<CntRequest> findAllEntities() {
        return cntRequestRepository.findAll();
    }

    @Override
    public @NotNull CntRequest createEntity(CntRequestInitializeModel initalizationModel) {
        return saveEntity(initalizationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return cntRequestRepository.findById(id).map(entity ->
        {
            cntRequestRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    @Override
    public @NotNull CntRequest saveEntity(@NotNull CntRequest entity) {
        return cntRequestRepository.save(entity);
    }

    @Override
    public Function<CntRequestModel, CntRequest> toEntity() {
        return cntModel -> cntRequestRepository.findById(cntModel.getId()).orElseThrow(() -> new EntityUnknownException(cntModel.getId()));
    }

    @Override
    public @NotNull Function<CntRequest, CntRequestModel> toModel() {
        return CntRequest::toModel;
    }

    public Optional<CntRequest> findByMessageId(Long messageId) {
        return cntRequestRepository.findByMessageId(messageId);
    }

    public List<CntRequest> findByUser(DiscordUser user) {
        return cntRequestRepository.findByUser(user);
    }

    public Page<CntRequest> getCntRequests(DiscordServer discordServer, int page, int size, String field, Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, field));
        return cntRequestRepository.findAllByDiscordServerOrderByTimeDesc(discordServer, pageable);
    }

    @Override
    public @NotNull CntRequest updateEntity(@NotNull CntRequest cntRequest, @NotNull CntRequestUpdateModel cntRequestUpdateModel) {
        if(cntRequestUpdateModel.getResetClaimer()) {
            cntRequest.setClaimer(null);
        }

        if(cntRequestUpdateModel.getClaimer() != null) {
            DiscordUser claimer = discordUserRepository.loadEntityOrCreate(cntRequestUpdateModel.getClaimer().getId());

            cntRequest.setClaimer(claimer);
        }

        if(cntRequestUpdateModel.getCoinValue() != null) {
            cntRequest.setCoinValue(cntRequestUpdateModel.getCoinValue());
        }

        if(cntRequestUpdateModel.getDescription() != null) {
            cntRequest.setDescription(cntRequestUpdateModel.getDescription());
        }

        if(cntRequestUpdateModel.getRequirement() != null) {
            cntRequest.setRequirement(cntRequestUpdateModel.getRequirement());
        }

        if(cntRequestUpdateModel.getCompleted() != null) {
            cntRequest.setCompleted(cntRequestUpdateModel.getCompleted());
        }

        return cntRequest;
    }
}