package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.common.enums.ScoreType;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.Score;
import me.taubsie.dungeonhub.server.entities.Server;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findScoreById_IdAndCarryTypeAndId_ScoreType(long id, CarryType carryType, ScoreType scoreType);

    List<Score> findScoresById_IdAndCarryType(long id, CarryType carryType);

    List<Score> findScoresById_IdAndCarryType_Server(long id, Server server);

    @NotNull Page<Score> findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(CarryType carryType, ScoreType scoreType, @NotNull Pageable pageable);
}