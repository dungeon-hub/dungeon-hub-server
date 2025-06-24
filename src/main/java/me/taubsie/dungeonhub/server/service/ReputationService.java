package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.Reputation;
import me.taubsie.dungeonhub.server.repositories.ReputationRepository;
import org.springframework.stereotype.Service;

@Service
public class ReputationService {

    private final ReputationRepository repRepository;

    public ReputationService(ReputationRepository reputationRepository) {
        this.repRepository = reputationRepository;
    }

    public Reputation findByUserId(int userID) {
        return repRepository.findByUserId(userID);
    }

    public Reputation save(Reputation reputation) {
        return repRepository.save(reputation);
    }
}
