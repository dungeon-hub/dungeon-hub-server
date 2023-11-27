package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarryTierRepository extends JpaRepository<CarryTier, Long> {
    Optional<CarryTier> findCarryTierByIdentifier(String identifier);

    Optional<CarryTier> findFirstByCarryType_DiscordServerAndCategory(DiscordServer discordServer, long category);

    List<CarryTier> findCarryTiersByCarryType(CarryType carryType);
}