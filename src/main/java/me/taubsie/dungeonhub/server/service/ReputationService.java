package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Reputation;
import me.taubsie.dungeonhub.server.repositories.DiscordUserRepository;
import me.taubsie.dungeonhub.server.repositories.ReputationRepository;
import org.springframework.stereotype.Service;

@Service
public class ReputationService {

    private final ReputationRepository repRepository;
    private final DiscordUserRepository userRepository;

    public ReputationService(ReputationRepository repRepository, DiscordUserRepository userRepository) {
        this.repRepository = repRepository;
        this.userRepository = userRepository;
    }

    public Reputation addReputation(Long userId, String repReason) {
        DiscordUser discordUser = userRepository.loadEntityOrCreate(userId);
        Reputation reputation = repRepository.findByUser(discordUser);
        if (reputation == null) {
            reputation = new Reputation(discordUser, 1, repReason);
        } else {
            reputation.updateRecord(1, repReason);
        }
        return repRepository.save(reputation);
    }

    public Reputation findByUserId(Long userId) {
        DiscordUser user = userRepository.loadEntityOrCreate(userId);
        if (user == null) {
            return null;
        } else {
            return repRepository.findByUser(user);
        }
    }
}
