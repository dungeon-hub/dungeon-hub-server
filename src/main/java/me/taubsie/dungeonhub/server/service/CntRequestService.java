package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.cnt_request.CntRequestCreationModel;
import me.taubsie.dungeonhub.common.model.cnt_request.CntRequestModel;
import me.taubsie.dungeonhub.common.model.cnt_request.CntRequestUpdateModel;
import me.taubsie.dungeonhub.server.entities.CntRequest;
import me.taubsie.dungeonhub.server.model.CntRequestInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CntRequestRepository;
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class CntRequestService implements EntityService<CntRequest, CntRequestModel, CntRequestCreationModel, CntRequestInitializeModel, CntRequestUpdateModel> {
    private final CntRequestRepository cntRequestRepository;
    private final RepositoryMethodInvocationListener repositoryMethodInvocationListener;

    @Override
    public Optional<CntRequest> loadEntityById(long id) {
        return cntRequestRepository.findById(id);
    }

    @Override
    public Optional<CntRequest> loadEntityByName(String name) {
        return Optional.empty();
    }

    @Override
    public List<CntRequest> findAllEntities() {
        return cntRequestRepository.findAll();
    }

    @Override
    public CntRequest createEntity(CntRequestInitializeModel initalizationModel) {
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
    public CntRequest saveEntity(CntRequest entity) {
        return cntRequestRepository.save(entity);
    }

    @Override
    public Function<CntRequestModel, CntRequest> toEntity() {
        return cntModel -> cntRequestRepository.findById(cntModel.getId()).orElseThrow(() -> new EntityUnknownException(cntModel.getId()));
    }

    @Override
    public Function<CntRequest, CntRequestModel> toModel() {
        return CntRequest::toModel;
    }

    public Optional<CntRequest> findByMessageId(Long messageId) {
        return cntRequestRepository.findByMessageId(messageId);
    }
}