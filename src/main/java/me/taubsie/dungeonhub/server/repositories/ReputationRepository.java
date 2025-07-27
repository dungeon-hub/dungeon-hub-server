package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Reputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReputationRepository extends JpaRepository<Reputation, Long> {
    @Query("SELECT SUM(rep.amount) FROM reputation rep where rep.discordServer = :discordServer and rep.user = :discordUser")
    long sumReputation(DiscordServer discordServer, DiscordUser discordUser);
}