package me.taubsie.dungeonhub.server.service;

import com.google.common.collect.Iterables;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.enums.ScoreResetType;
import me.taubsie.dungeonhub.common.enums.ScoreType;
import me.taubsie.dungeonhub.common.model.ScoreResetModel;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.repositories.ScoreRepository;
import me.taubsie.dungeonhub.server.repositories.ScoreSumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@NoArgsConstructor
public class ScoreService {
    private static final int PAGE_SIZE = 10;
    private ScoreRepository scoreRepository;
    private ScoreSumRepository scoreSumRepository;

    @Autowired
    @Lazy
    public ScoreService(ScoreRepository scoreRepository, ScoreSumRepository scoreSumRepository) {
        this.scoreRepository = scoreRepository;
        this.scoreSumRepository = scoreSumRepository;
    }

    public List<Score> getAll() {
        return scoreRepository.findAll();
    }

    public Optional<Score> countScoreForCarrier(DiscordUser carrier, CarryType carryType, ScoreType scoreType) {
        return scoreRepository.findScoreByCarrierAndCarryTypeAndId_ScoreType(carrier, carryType, scoreType);
    }

    public Optional<ScoreSum> countTotalScoreForCarrier(DiscordUser carrier, DiscordServer discordServer, ScoreType scoreType) {
        return scoreSumRepository.findScoreByCarrierAndServerAndId_ScoreType(carrier, discordServer, scoreType);
    }

    public Page<Score> getFullLeaderboard(CarryType carryType, ScoreType scoreType) {
        return scoreRepository.findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(carryType, scoreType,
                Pageable.unpaged());
    }

    public Page<ScoreSum> getFullTotalLeaderboard(DiscordServer discordServer, ScoreType scoreType) {
        return scoreSumRepository.findAllByServerAndId_ScoreTypeOrderByTotalScoreDesc(discordServer, scoreType,
                Pageable.unpaged());
    }

    public Page<Score> getLeaderboard(CarryType carryType, ScoreType scoreType) {
        return scoreRepository.findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(carryType, scoreType,
                PageRequest.ofSize(PAGE_SIZE));
    }

    public Page<ScoreSum> getTotalLeaderboard(DiscordServer discordServer, ScoreType scoreType) {
        return scoreSumRepository.findAllByServerAndId_ScoreTypeOrderByTotalScoreDesc(discordServer, scoreType, PageRequest.ofSize(PAGE_SIZE));
    }

    public Page<Score> getLeaderboard(CarryType carryType, ScoreType scoreType, int page) {
        return scoreRepository.findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(carryType, scoreType,
                PageRequest.of(page, PAGE_SIZE));
    }

    public Page<ScoreSum> getTotalLeaderboard(DiscordServer discordServer, ScoreType scoreType, int page) {
        return scoreSumRepository.findAllByServerAndId_ScoreTypeOrderByTotalScoreDesc(discordServer, scoreType,
                PageRequest.of(page, PAGE_SIZE));
    }

    public int getPosition(CarryType carryType, ScoreType scoreType, DiscordUser carrier) {
        return Iterables.indexOf(
                getFullLeaderboard(carryType, scoreType),
                score -> carrier.getId() == score.getId().getId()
        );
    }

    public int getTotalPosition(DiscordServer discordServer, ScoreType scoreType, DiscordUser carrier) {
        return Iterables.indexOf(
                getFullTotalLeaderboard(discordServer, scoreType),
                score -> carrier.getId() == score.getId().getId()
        );
    }

    public List<Score> getAllScores(CarryType carryType) {
        return scoreRepository.findScoresByCarryType(carryType);
    }

    public List<Score> getAllScores(DiscordUser carrier, CarryType carryType) {
        return scoreRepository.findScoresByCarrierAndCarryType(carrier, carryType);
    }

    public List<Score> getAllScores(DiscordUser carrier, DiscordServer discordServer) {
        return scoreRepository.findScoresByCarrierAndCarryType_DiscordServer(carrier, discordServer);
    }

    public List<Score> updateAllScores(DiscordUser carrier, CarryType carryType, long amount) {
        return Arrays.stream(ScoreType.values())
                .map(scoreType -> updateScore(carrier, carryType, scoreType, amount))
                .toList();
    }

    public Score updateScore(DiscordUser carrier, CarryType carryType, ScoreType scoreType, long amount) {
        Score score = scoreRepository.findScoreByCarrierAndCarryTypeAndId_ScoreType(carrier, carryType, scoreType)
                .orElseGet(() -> new Score(
                        new ScoreId(carrier.getId(), carryType.getId(), scoreType),
                        carryType,
                        carrier,
                        0L
                ));

        score.setScoreAmount(score.getScoreAmount() + amount);

        return scoreRepository.save(score);
    }

    public ScoreResetModel resetScores(CarryType carryType, ScoreResetType scoreResetType) {
        List<Score> scores = getAllScores(carryType);

        scores = applyScoreResetType(scores, scoreResetType);

        List<Score> updatedScores = scoreRepository.saveAll(scores);

        return new ScoreResetModel(
                updatedScores.stream().filter(score -> score.getId().getScoreType() == ScoreType.DEFAULT).count(),
                updatedScores.stream().filter(score -> score.getId().getScoreType() == ScoreType.EVENT).count()
        );
    }

    private List<Score> applyScoreResetType(List<Score> scores, ScoreResetType scoreResetType) {
        Stream<Score> currentScores = scores.parallelStream();

        currentScores = switch (scoreResetType) {
            case Both -> currentScores.filter(score -> score.getId().getScoreType() != ScoreType.ALLTIME);
            case Event -> currentScores.filter(score -> score.getId().getScoreType() == ScoreType.EVENT);
            case Default -> currentScores.filter(score -> score.getId().getScoreType() == ScoreType.DEFAULT);
        };

        return currentScores.map(score -> {
            score.setScoreAmount(0L);
            return score;
        }).toList();
    }
}