package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyHistoryRepository;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
import me.taubsie.dungeonhub.server.repositories.CarryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CarryServiceTest {
    private static final Instant JANUARY = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant FEBRUARY = Instant.parse("2025-02-01T00:00:00Z");
    private static final Instant MARCH = Instant.parse("2025-03-01T00:00:00Z");

    private CarryDifficultyHistoryRepository historyRepository;
    private CarryService service;

    @BeforeEach
    void setUp() {
        historyRepository = mock(CarryDifficultyHistoryRepository.class);
        CarryDifficultyService difficultyService =
                new CarryDifficultyService(mock(CarryDifficultyRepository.class), historyRepository);
        service = new CarryService(mock(CarryRepository.class), difficultyService);
    }

    @Test
    void carriesArePricedWithTheValuesValidAtTheirTime() {
        CarryDifficulty difficulty = difficulty(1, 300);
        when(historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L))).thenReturn(List.of(
                new CarryDifficultyHistory(difficulty, 100, null, null, 1, JANUARY, FEBRUARY),
                new CarryDifficultyHistory(difficulty, 200, null, null, 1, FEBRUARY, MARCH)));
        Carry inJanuary = carry(difficulty, 2, FEBRUARY.minusSeconds(1));
        Carry atChange = carry(difficulty, 2, FEBRUARY);
        Carry afterLastChange = carry(difficulty, 2, MARCH);

        ToLongFunction<Carry> calculator = service.historicalPriceCalculator(List.of(inJanuary, atChange, afterLastChange));

        assertEquals(200, calculator.applyAsLong(inJanuary));
        assertEquals(400, calculator.applyAsLong(atChange));
        assertEquals(600, calculator.applyAsLong(afterLastChange));
    }

    @Test
    void historicalBulkPricingIsApplied() {
        CarryDifficulty difficulty = difficulty(1, 300);
        when(historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L))).thenReturn(List.of(
                new CarryDifficultyHistory(difficulty, 100, 80, 5, 1, JANUARY, FEBRUARY)));
        Carry belowBulkAmount = carry(difficulty, 4, JANUARY);
        Carry aboveBulkAmount = carry(difficulty, 10, JANUARY);

        ToLongFunction<Carry> calculator = service.historicalPriceCalculator(List.of(belowBulkAmount, aboveBulkAmount));

        assertEquals(400, calculator.applyAsLong(belowBulkAmount));
        assertEquals(800, calculator.applyAsLong(aboveBulkAmount));
    }

    @Test
    void historyOfOneDifficultyIsNotUsedForAnother() {
        CarryDifficulty withHistory = difficulty(1, 300);
        CarryDifficulty withoutHistory = difficulty(2, 50);
        when(historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L, 2L))).thenReturn(List.of(
                new CarryDifficultyHistory(withHistory, 100, null, null, 1, JANUARY, FEBRUARY)));
        Carry carry = carry(withoutHistory, 1, JANUARY);

        ToLongFunction<Carry> calculator = service.historicalPriceCalculator(
                List.of(carry, carry(withHistory, 1, JANUARY)));

        assertEquals(50, calculator.applyAsLong(carry));
    }

    @Test
    void carryWithoutTimestampUsesTheCurrentPrice() {
        CarryDifficulty difficulty = difficulty(1, 300);
        when(historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L))).thenReturn(List.of(
                new CarryDifficultyHistory(difficulty, 100, null, null, 1, JANUARY, FEBRUARY)));
        Carry carry = carry(difficulty, 1, null);

        assertEquals(300, service.historicalPriceCalculator(List.of(carry)).applyAsLong(carry));
    }

    @Test
    void historyIsLoadedOnceForAllCarries() {
        CarryDifficulty first = difficulty(1, 100);
        CarryDifficulty second = difficulty(2, 100);
        List<Carry> carries = List.of(carry(first, 1, JANUARY), carry(first, 1, FEBRUARY), carry(second, 1, MARCH));

        ToLongFunction<Carry> calculator = service.historicalPriceCalculator(carries);
        carries.forEach(calculator::applyAsLong);

        verify(historyRepository, times(1)).findAllByCarryDifficulty_IdIn(Set.of(1L, 2L));
        verifyNoMoreInteractions(historyRepository);
    }

    @Test
    void noCarriesDoNotQueryHistory() {
        service.historicalPriceCalculator(List.of());

        verify(historyRepository, never()).findAllByCarryDifficulty_IdIn(any());
    }

    private static CarryDifficulty difficulty(long id, int price) {
        return new CarryDifficulty(id, "difficulty-" + id, "Difficulty " + id, null, null, null, null, null, price, 1,
                null);
    }

    private static Carry carry(CarryDifficulty difficulty, int amount, Instant time) {
        return new Carry(null, null, amount, difficulty, null, time);
    }
}
