package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.repositories.CarryRepository;
import net.dungeonhub.model.stats.GlobalCarrierStatsModel;
import net.dungeonhub.model.stats.GlobalCarryStatsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CarryService {
    private final CarryRepository carryRepository;

    @Autowired
    public CarryService(CarryRepository carryRepository) {
        this.carryRepository = carryRepository;
    }

    public int countCarries(DiscordServer server, DiscordUser user) {
        return carryRepository.countCarryByCarryDifficulty_CarryTier_CarryType_DiscordServerAndCarrier(server, user);
    }

    @Transactional(readOnly = true)
    public List<Carry> getCarries(DiscordServer server) {
        return carryRepository.getCarriesByCarryDifficulty_CarryTier_CarryType_DiscordServer(server);
    }

    public List<Carry> getCarriesSince(DiscordServer server, Instant instant) {
        return carryRepository.getCarriesByCarryDifficulty_CarryTier_CarryType_DiscordServerAndTimeGreaterThanEqual(server, instant);
    }

    public GlobalCarryStatsModel getGlobalCarryStats() {
        return new GlobalCarryStatsModel(
                carryRepository.sumLifetimeCarries(),
                carryRepository.sumCarriesByTime(Instant.now().minus(60, ChronoUnit.DAYS)),
                carryRepository.sumCarriesByTime(Instant.now().minus(30, ChronoUnit.DAYS)),
                carryRepository.sumCarriesByTime(Instant.now().minus(14, ChronoUnit.DAYS)),
                carryRepository.sumCarriesByTime(Instant.now().minus(7, ChronoUnit.DAYS))
        );
    }

    public GlobalCarrierStatsModel getGlobalCarrierStats() {
        return new GlobalCarrierStatsModel(
                carryRepository.countLifetimeCarriers(),
                carryRepository.countCarriersByTimespan(Instant.now().minus(60, ChronoUnit.DAYS), Instant.now().minus(30, ChronoUnit.DAYS)),
                carryRepository.countCarriersByTime(Instant.now().minus(30, ChronoUnit.DAYS)),
                carryRepository.countCarriersByTimespan(Instant.now().minus(14, ChronoUnit.DAYS), Instant.now().minus(7, ChronoUnit.DAYS)),
                carryRepository.countCarriersByTime(Instant.now().minus(7, ChronoUnit.DAYS))
        );
    }

    public long getCarriersByDiscordServer(DiscordServer discordServer) {
        return carryRepository.countCarriersByDiscordServer(discordServer);
    }

    public Carry saveCarry(Carry carry) {
        return carryRepository.save(carry);
    }
}