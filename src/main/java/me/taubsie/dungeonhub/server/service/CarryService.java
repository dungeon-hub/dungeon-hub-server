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
     * Builds a reusable calculator that prices carries with the values that were valid when each carry occurred.
     *
     * <p>The method extracts the distinct difficulties from {@code carries} and loads their complete price histories
     * in one repository call. The returned function closes over that in-memory history map, so applying it repeatedly
     * during one endpoint request does not execute additional history queries. Callers should therefore create the
     * calculator once, before starting their aggregations, and reuse the same instance for every sum or filtered sum
     * derived from that carry list.</p>
     *
     * <p>History periods use an inclusive {@code dateFrom} and exclusive {@code dateTo}. When a carry has no timestamp,
     * or its timestamp is not covered by a closed history period, the current values on its {@link CarryDifficulty}
     * are used. This fallback is intentional: the current values have no history row until they are replaced.</p>
     *
     * @param carries all carries that may be passed to the returned calculator; passing additional carries later may
     *                fall back to current pricing because their difficulty history was not preloaded
     * @return a request-local function suitable for {@link java.util.stream.LongStream} aggregations via
     *         {@code carries.stream().mapToLong(calculator)}
     */
    @Transactional(readOnly = true)
    public ToLongFunction<Carry> historicalPriceCalculator(List<Carry> carries) {
        List<CarryDifficulty> difficulties = carries.stream()
                .map(Carry::getCarryDifficulty)
                .distinct()
                .toList();
        Map<CarryDifficulty, List<CarryDifficultyHistory>> histories = carryDifficultyService
                .loadPriceHistory(difficulties)
                .stream()
                .collect(Collectors.groupingBy(CarryDifficultyHistory::getCarryDifficulty));

        return carry -> histories.getOrDefault(carry.getCarryDifficulty(), List.of()).stream()
                .filter(history -> carry.getTime() != null && history.includes(carry.getTime()))
                .findFirst()
                .map(carry::calculateTotalPrice)
                .orElseGet(carry::calculateTotalPrice);
    }
}
