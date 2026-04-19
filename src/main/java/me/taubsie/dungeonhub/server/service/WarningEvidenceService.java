package me.taubsie.dungeonhub.server.service;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.model.WarningEvidenceInitializeModel;
import me.taubsie.dungeonhub.server.repositories.WarningEvidenceRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WarningEvidenceService {
    private final WarningEvidenceRepository warningEvidenceRepository;

    public void create(WarningEvidenceInitializeModel initializeModel) {
        warningEvidenceRepository.save(initializeModel.toEntity());
    }
}