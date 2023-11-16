package me.taubsie.dungeonhub.server.service;

import com.google.common.collect.Iterables;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.enums.ScoreType;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.Score;
import me.taubsie.dungeonhub.server.entities.ScoreId;
import me.taubsie.dungeonhub.server.entities.Server;
import me.taubsie.dungeonhub.server.repositories.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@NoArgsConstructor
public class ScoreService {
    private static final int PAGE_SIZE = 10;
    private ScoreRepository scoreRepository;

    @Autowired
    @Lazy
    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    public List<Score> getAll() {
        return scoreRepository.findAll();
    }

    public Optional<Score> countScoreForCarrier(long carrierId, CarryType carryType, ScoreType scoreType) {
        return scoreRepository.findScoreById_IdAndCarryTypeAndId_ScoreType(carrierId, carryType, scoreType);
    }

    public Page<Score> getFullLeaderboard(CarryType carryType, ScoreType scoreType) {
        return scoreRepository.findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(carryType, scoreType,
                Pageable.unpaged());
    }

    public Page<Score> getLeaderboard(CarryType carryType, ScoreType scoreType) {
        return scoreRepository.findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(carryType, scoreType,
                PageRequest.ofSize(PAGE_SIZE));
    }

    public Page<Score> getLeaderboard(CarryType carryType, ScoreType scoreType, int page) {
        return scoreRepository.findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(carryType, scoreType,
                PageRequest.of(page, PAGE_SIZE));
    }

    public int getPosition(CarryType carryType, ScoreType scoreType, long carrierId) {
        return Iterables.indexOf(
                scoreRepository.findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(carryType, scoreType,
                        Pageable.unpaged()),
                score -> carrierId == score.getId().getId()
        );
    }

    public List<Score> getAllScores(long carrierId, CarryType carryType) {
        return scoreRepository.findScoresById_IdAndCarryType(carrierId, carryType);
    }

    public List<Score> getAllScores(long carrierId, Server server) {
        return scoreRepository.findScoresById_IdAndCarryType_Server(carrierId, server);
    }

    public List<Score> updateAllScores(long carrierId, CarryType carryType, long amount) {
        return Arrays.stream(ScoreType.values())
                .map(scoreType -> updateScore(carrierId, carryType, scoreType, amount))
                .toList();
    }

    public Score updateScore(long carrierId, CarryType carryType, ScoreType scoreType, long amount) {
        Score score = scoreRepository.findScoreById_IdAndCarryTypeAndId_ScoreType(carrierId, carryType, scoreType)
                .orElseGet(() -> new Score(
                        new ScoreId(carrierId, carryType.getId(), scoreType),
                        carryType,
                        0L
                ));

        score.setScoreAmount(score.getScoreAmount() + amount);

        return scoreRepository.save(score);
    }
}