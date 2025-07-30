package me.taubsie.dungeonhub.server.service;

import com.google.common.collect.Iterables;
import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Reputation;
import me.taubsie.dungeonhub.server.entities.ReputationSum;
import me.taubsie.dungeonhub.server.model.ReputationInitializeModel;
import me.taubsie.dungeonhub.server.repositories.ReputationRepository;
import net.dungeonhub.exceptions.EntityUnknownException;
import net.dungeonhub.model.reputation.ReputationCreationModel;
import net.dungeonhub.model.reputation.ReputationModel;
import net.dungeonhub.model.reputation.ReputationUpdateModel;
import net.dungeonhub.structure.entity.EntityService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class ReputationService implements EntityService<Reputation, ReputationModel, ReputationCreationModel, ReputationInitializeModel, ReputationUpdateModel> {
    private static final int PAGE_SIZE = 10;
    private final ReputationRepository reputationRepository;

    @NotNull
    @Override
    public Optional<Reputation> loadEntityById(long id) {
        return reputationRepository.findById(id);
    }

    @NotNull
    @Override
    public List<Reputation> findAllEntities() {
        return reputationRepository.findAll();
    }

    @NotNull
    @Override
    public Reputation createEntity(@NotNull ReputationInitializeModel reputationInitializeModel) {
        return saveEntity(reputationInitializeModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return reputationRepository.findById(id).map(entity ->
        {
            reputationRepository.delete(entity);
            return true;
        }).orElse(false);
    }

    @NotNull
    @Override
    public Reputation saveEntity(@NotNull Reputation reputation) {
        return reputationRepository.save(reputation);
    }

    @Nullable
    @Override
    public Function<ReputationModel, Reputation> toEntity() {
        return reputationModel -> reputationRepository.findById(reputationModel.getId()).orElseThrow(() -> new EntityUnknownException(reputationModel.getId()));
    }

    @NotNull
    @Override
    public Function<Reputation, ReputationModel> toModel() {
        return Reputation::toModel;
    }

    @NotNull
    @Override
    public Reputation updateEntity(@NotNull Reputation reputation, @NotNull ReputationUpdateModel reputationUpdateModel) {
        if(reputationUpdateModel.getReason() != null) {
            reputation.setReason(reputationUpdateModel.getReason());
        }

        if(reputationUpdateModel.getAmount() != null) {
            reputation.setAmount(reputationUpdateModel.getAmount());
        }

        return reputation;
    }

    public Long calculateReputation(DiscordServer discordServer, DiscordUser discordUser) {
        return reputationRepository.sumReputation(discordServer, discordUser);
    }

    public Page<ReputationSum> getReputationLeaderboard(DiscordServer discordServer, int page) {
        return reputationRepository.findAllReputations(discordServer, PageRequest.of(page, PAGE_SIZE));
    }

    public Page<ReputationSum> getFullReputationLeaderboard(DiscordServer discordServer) {
        return reputationRepository.findAllReputations(discordServer, Pageable.unpaged());
    }

    public int getPosition(DiscordServer discordServer, DiscordUser user) {
        return Iterables.indexOf(
                getFullReputationLeaderboard(discordServer),
                rep -> user.getId() == rep.discordUser().getId()
        );
    }

    public List<Reputation> getAllReputations(DiscordServer discordServer, DiscordUser user) {
        return reputationRepository.findReputationsByDiscordServerAndUser(discordServer, user);
    }
}