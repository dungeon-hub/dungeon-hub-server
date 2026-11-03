package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.DungeonHubServerApplication;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyHistoryRepository;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
import me.taubsie.dungeonhub.server.repositories.CarryRepository;
import net.dungeonhub.model.carry.CarryCountsModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// application.properties requires an external config file, so it is skipped here and the H2 migrations are used directly
@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/none/",
        "spring.flyway.locations=classpath:migrations/{vendor}",
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = DungeonHubServerApplication.class)
class CarryServiceTest {
    private static final Instant JANUARY = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant FEBRUARY = Instant.parse("2025-02-01T00:00:00Z");
    private static final Instant MARCH = Instant.parse("2025-03-01T00:00:00Z");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarryRepository carryRepository;

    private CarryDifficultyHistoryRepository historyRepository;
    private CarryService carryService;

    private DiscordServer server;
    private DiscordUser carrier;
    private DiscordUser player;

    @BeforeEach
    void setUp() {
        historyRepository = mock(CarryDifficultyHistoryRepository.class);
        CarryDifficultyService difficultyService =
                new CarryDifficultyService(mock(CarryDifficultyRepository.class), historyRepository);
        carryService = new CarryService(carryRepository, difficultyService);

        server = entityManager.persist(new DiscordServer(1L));
        carrier = entityManager.persist(new DiscordUser(10L));
        player = entityManager.persist(new DiscordUser(11L));
    }

    @Test
    void getCarryCounts_sumsAmountPerCarryType() {
        CarryType dungeons = entityManager.persist(new CarryType("dungeons", "Dungeons", server, null, null));
        CarryType slayer = entityManager.persist(new CarryType("slayer", "Slayer", server, null, null));
        CarryType unused = entityManager.persist(new CarryType("kuudra", "Kuudra", server, null, null));

        CarryDifficulty f7 = createDifficulty(dungeons, "f7");
        CarryDifficulty m7 = createDifficulty(dungeons, "m7");
        CarryDifficulty t4 = createDifficulty(slayer, "t4");
        createDifficulty(unused, "t5");

        logCarry(carrier, f7, 5);
        logCarry(carrier, m7, 2);
        logCarry(carrier, t4, 3);
        logCarry(player, t4, 7);

        DiscordServer otherServer = entityManager.persist(new DiscordServer(2L));
        CarryType otherType = entityManager.persist(new CarryType("dungeons", "Dungeons", otherServer, null, null));
        logCarry(carrier, createDifficulty(otherType, "f7"), 11);

        entityManager.flush();

        CarryCountsModel carryCounts = carryService.getCarryCounts(server, carrier);

        assertEquals(3, carryCounts.getLogCount());
        assertEquals(10, carryCounts.getCarryCount());
        assertEquals(Map.of(dungeons.getId(), 7L, slayer.getId(), 3L), carryCounts.getByType());
        // the existing total still counts carry logs, not amounts
        assertEquals(3, carryService.countCarries(server, carrier));
    }

    @Test
    void getCarryCounts_returnsZeroWithoutCarries() {
        CarryCountsModel carryCounts = carryService.getCarryCounts(server, carrier);

        assertEquals(0, carryCounts.getLogCount());
        assertEquals(0, carryCounts.getCarryCount());
        assertTrue(carryCounts.getByType().isEmpty());
    }

    @Test
    void carriesArePricedWithTheValuesValidAtTheirTime() {
        CarryDifficulty difficulty = difficulty(1, 300);
        when(historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L))).thenReturn(List.of(
                new CarryDifficultyHistory(difficulty, 100, List.of(), 1, JANUARY, FEBRUARY),
                new CarryDifficultyHistory(difficulty, 200, List.of(), 1, FEBRUARY, MARCH)));
        Carry inJanuary = carry(difficulty, 2, FEBRUARY.minusSeconds(1));
        Carry atChange = carry(difficulty, 2, FEBRUARY);
        Carry afterLastChange = carry(difficulty, 2, MARCH);

        ToLongFunction<Carry> calculator = carryService.historicalPriceCalculator(List.of(inJanuary, atChange, afterLastChange));

        assertEquals(200, calculator.applyAsLong(inJanuary));
        assertEquals(400, calculator.applyAsLong(atChange));
        assertEquals(600, calculator.applyAsLong(afterLastChange));
    }

    @Test
    void historicalBulkPricingIsApplied() {
        CarryDifficulty difficulty = difficulty(1, 300);
        when(historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L))).thenReturn(List.of(
                new CarryDifficultyHistory(difficulty, 100, List.of(new CarryDifficultyHistoryBulkPrice(5, 80)), 1, JANUARY, FEBRUARY)));
        Carry belowBulkAmount = carry(difficulty, 4, JANUARY);
        Carry aboveBulkAmount = carry(difficulty, 10, JANUARY);

        ToLongFunction<Carry> calculator = carryService.historicalPriceCalculator(List.of(belowBulkAmount, aboveBulkAmount));

        assertEquals(400, calculator.applyAsLong(belowBulkAmount));
        assertEquals(800, calculator.applyAsLong(aboveBulkAmount));
    }

    @Test
    void historyOfOneDifficultyIsNotUsedForAnother() {
        CarryDifficulty withHistory = difficulty(1, 300);
        CarryDifficulty withoutHistory = difficulty(2, 50);
        when(historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L, 2L))).thenReturn(List.of(
                new CarryDifficultyHistory(withHistory, 100, List.of(), 1, JANUARY, FEBRUARY)));
        Carry carry = carry(withoutHistory, 1, JANUARY);

        ToLongFunction<Carry> calculator = carryService.historicalPriceCalculator(
                List.of(carry, carry(withHistory, 1, JANUARY)));

        assertEquals(50, calculator.applyAsLong(carry));
    }

    @Test
    void carryWithoutTimestampUsesTheCurrentPrice() {
        CarryDifficulty difficulty = difficulty(1, 300);
        when(historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L))).thenReturn(List.of(
                new CarryDifficultyHistory(difficulty, 100, List.of(), 1, JANUARY, FEBRUARY)));
        Carry carry = carry(difficulty, 1, null);

        assertEquals(300, carryService.historicalPriceCalculator(List.of(carry)).applyAsLong(carry));
    }

    @Test
    void historyIsLoadedOnceForAllCarries() {
        CarryDifficulty first = difficulty(1, 100);
        CarryDifficulty second = difficulty(2, 100);
        List<Carry> carries = List.of(carry(first, 1, JANUARY), carry(first, 1, FEBRUARY), carry(second, 1, MARCH));

        ToLongFunction<Carry> calculator = carryService.historicalPriceCalculator(carries);
        carries.forEach(calculator::applyAsLong);

        verify(historyRepository, times(1)).findAllByCarryDifficulty_IdIn(Set.of(1L, 2L));
        verifyNoMoreInteractions(historyRepository);
    }

    @Test
    void noCarriesDoNotQueryHistory() {
        carryService.historicalPriceCalculator(List.of());

        verify(historyRepository, never()).findAllByCarryDifficulty_IdIn(any());
    }

    private CarryDifficulty createDifficulty(CarryType carryType, String identifier) {
        CarryTier carryTier = entityManager.persist(new CarryTier(identifier, identifier, carryType, null, null, null, null, null));
        return entityManager.persist(new CarryDifficulty(identifier, identifier, carryTier, null, null, null, 0, 1, null));
    }

    private void logCarry(DiscordUser carryCarrier, CarryDifficulty carryDifficulty, int amount) {
        entityManager.persist(new Carry(carryCarrier, player, amount, carryDifficulty, null, Instant.now()));
    }

    private static CarryDifficulty difficulty(long id, int price) {
        return new CarryDifficulty(id, "difficulty-" + id, "Difficulty " + id, null, null, null, null, price, 1,
                null);
    }

    private static Carry carry(CarryDifficulty difficulty, int amount, Instant time) {
        return new Carry(null, null, amount, difficulty, null, time);
    }
}
