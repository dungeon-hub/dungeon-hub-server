package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.common.enums.ScoreType;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Score;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findScoreByCarrierAndCarryTypeAndId_ScoreType(DiscordUser carrier, CarryType carryType, ScoreType scoreType);

    List<Score> findScoresByCarryType(CarryType carryType);

    List<Score> findScoresByCarrierAndCarryType(DiscordUser carrier, CarryType carryType);

    List<Score> findScoresByCarrierAndCarryType_DiscordServer(DiscordUser carrier, DiscordServer discordServer);

    default @NotNull Page<Score> findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(CarryType carryType, ScoreType scoreType, @NotNull Pageable pageable) {
        return findAllByCarryTypeAndId_ScoreTypeAndScoreAmountGreaterThanOrderByScoreAmountDesc(carryType, scoreType, 0L, pageable);
    }

    /**
     * This returns all score values that are over a given threshold. This is needed to make it possible to hide 0 score values. Other than that, it isn't really needed.
     *
     * @param carryType   The carry type that belongs to the score values.
     * @param scoreType   The score type that belongs to the score values.
     * @param greaterThan The threshold which the score value should at least be.
     * @param pageable    The object that defines how the page should be rendered.
     * @return A page of the scores that were selected.
     */
    @NotNull
    Page<Score> findAllByCarryTypeAndId_ScoreTypeAndScoreAmountGreaterThanOrderByScoreAmountDesc(CarryType carryType, ScoreType scoreType, Long greaterThan, @NotNull Pageable pageable);
}