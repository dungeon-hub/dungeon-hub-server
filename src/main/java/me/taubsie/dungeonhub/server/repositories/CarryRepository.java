package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface CarryRepository extends JpaRepository<Carry, Long> {
    int countCarryByCarryDifficulty_CarryTier_CarryType_DiscordServerAndCarrier(DiscordServer server, DiscordUser carrier);

    List<Carry> getCarriesByCarryDifficulty_CarryTier_CarryType_DiscordServer(DiscordServer server);

    List<Carry> getCarriesByCarryDifficulty_CarryTier_CarryType_DiscordServerAndTimeGreaterThanEqual(DiscordServer server, Instant instant);

    @Query("select coalesce(sum(c.amount), 0) from carry c")
    long sumLifetimeCarries();

    @Query("select coalesce(sum(c.amount), 0) from carry c where c.time >= :time")
    long sumCarriesByTime(Instant time);

    @Query("select count(distinct c.carrier) from carry c")
    long countLifetimeCarriers();

    @Query("select count(distinct c.carrier) from carry c where c.time >= :time")
    long countCarriersByTime(Instant time);

    @Query("select count(distinct c.carrier) from carry c where c.time >= :after and c.time < :before")
    long countCarriersByTimespan(Instant after, Instant before);

    @Query("select count(distinct c.carrier) from carry c where c.carryDifficulty.carryTier.carryType.discordServer = :discordServer")
    long countCarriersByDiscordServer(DiscordServer discordServer);
}
