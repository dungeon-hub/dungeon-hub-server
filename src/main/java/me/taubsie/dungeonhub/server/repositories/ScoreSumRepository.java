package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.ScoreSum;
import net.dungeonhub.enums.ScoreType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreSumRepository extends JpaRepository<ScoreSum, Long> {
    Optional<ScoreSum> findScoreByCarrierAndServerAndId_ScoreType(DiscordUser carrier, DiscordServer discordServer, ScoreType scoreType);

    @NotNull
    Page<ScoreSum> findAllByServerAndId_ScoreTypeAndTotalScoreGreaterThanOrderByTotalScoreDesc(DiscordServer discordServer, ScoreType scoreType, Long greaterThan, @NotNull Pageable pageable);

    default @NotNull Page<ScoreSum> findAllByServerAndId_ScoreTypeOrderByTotalScoreDesc(DiscordServer discordServer, ScoreType scoreType, @NotNull Pageable pageable) {
        return findAllByServerAndId_ScoreTypeAndTotalScoreGreaterThanOrderByTotalScoreDesc(discordServer, scoreType, 0L, pageable);
    }
}