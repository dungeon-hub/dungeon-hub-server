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

    @NotNull Page<Score> findAllByCarryTypeAndId_ScoreTypeOrderByScoreAmountDesc(CarryType carryType, ScoreType scoreType, @NotNull Pageable pageable);
}