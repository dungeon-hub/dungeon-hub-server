package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

@Service
public class CarryService {
    private final CarryRepository carryRepository;
    private final CarryDifficultyService carryDifficultyService;

    @Autowired
    public CarryService(CarryRepository carryRepository, CarryDifficultyService carryDifficultyService) {
        this.carryRepository = carryRepository;
        this.carryDifficultyService = carryDifficultyService;
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

    /**
     * Builds a function that prices carries with the values that were valid at the time of each carry.
     *
     * <p>The history of all difficulties used by {@code carries} is loaded once up front, so the returned function can
     * be applied to every carry of the list without additional queries. Carries without a timestamp, or with a
     * timestamp that is not covered by the history, are priced with the current values of their difficulty.</p>
     */
    @Transactional(readOnly = true)
    public ToLongFunction<Carry> historicalPriceCalculator(List<Carry> carries) {
        Set<Long> carryDifficultyIds = carries.stream()
                .map(Carry::getCarryDifficulty)
                .filter(Objects::nonNull)
                .map(CarryDifficulty::getId)
                .collect(Collectors.toSet());

        Map<Long, List<CarryDifficultyHistory>> historyByDifficulty = carryDifficultyService
                .loadPriceHistory(carryDifficultyIds)
                .stream()
                .collect(Collectors.groupingBy(history -> history.getCarryDifficulty().getId()));

        return carry -> findHistoryAtTimeOfCarry(historyByDifficulty, carry)
                .map(history -> history.calculateTotalPrice(carry.getAmount()))
                .orElseGet(carry::calculateTotalPrice);
    }

    private static Optional<CarryDifficultyHistory> findHistoryAtTimeOfCarry(
            Map<Long, List<CarryDifficultyHistory>> historyByDifficulty, Carry carry) {
        if (carry.getTime() == null || carry.getCarryDifficulty() == null) {
            return Optional.empty();
        }

        return historyByDifficulty.getOrDefault(carry.getCarryDifficulty().getId(), List.of()).stream()
                .filter(history -> history.includes(carry.getTime()))
                .findFirst();
    }
}
