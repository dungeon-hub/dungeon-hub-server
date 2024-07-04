package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarryRepository extends JpaRepository<Carry, Long> {
    int countCarryByCarryDifficulty_CarryTier_CarryType_DiscordServerAndCarrier(DiscordServer server, DiscordUser carrier);
}
