package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Reputation;
import me.taubsie.dungeonhub.server.entities.ReputationSum;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReputationRepository extends JpaRepository<Reputation, Long> {
    @Nullable
    @Query("SELECT SUM(rep.amount) FROM reputation rep where rep.discordServer = :discordServer and rep.user = :discordUser and rep.active = true")
    Long sumReputation(DiscordServer discordServer, DiscordUser discordUser);

    @Query(
            value = "select new me.taubsie.dungeonhub.server.entities.ReputationSum(r.user, SUM(r.amount)) from reputation r where r.discordServer = :server and r.active = true group by r.user order by sum(r.amount) desc",
            countQuery = "select count(distinct r.user) from reputation r where r.discordServer = :server and r.active = true"
    )
    Page<ReputationSum> findAllReputations(@Param("server") DiscordServer discordServer, Pageable pageable);

    List<Reputation> findReputationsByDiscordServerAndUser(DiscordServer discordServer, DiscordUser discordUser);
}