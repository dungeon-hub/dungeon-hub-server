package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.common.enums.ScoreType;
import me.taubsie.dungeonhub.server.entities.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreSumRepository extends JpaRepository<ScoreSum, Long> {
    Optional<ScoreSum> findScoreByCarrierAndServerAndId_ScoreType(DiscordUser carrier, DiscordServer discordServer, ScoreType scoreType);

    @NotNull
    Page<ScoreSum> findAllByServerAndId_ScoreTypeOrderByTotalScoreDesc(DiscordServer discordServer, ScoreType scoreType, @NotNull Pageable pageable);
}