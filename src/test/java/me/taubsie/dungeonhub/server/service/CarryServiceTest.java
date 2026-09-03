package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import me.taubsie.dungeonhub.server.repositories.CarryRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CarryServiceTest {
    @Test
    void historicalCalculatorLoadsHistoryOnceAndReusesIt() {
        CarryRepository carryRepository = mock(CarryRepository.class);
        CarryDifficultyService difficultyService = mock(CarryDifficultyService.class);
        CarryService service = new CarryService(carryRepository, difficultyService);
        CarryDifficulty difficulty = mock(CarryDifficulty.class);
        Carry first = mock(Carry.class);
        Carry second = mock(Carry.class);
        Carry current = mock(Carry.class);
        Instant changedAt = Instant.parse("2025-02-01T00:00:00Z");
        CarryDifficultyHistory oldPrice = new CarryDifficultyHistory(
                difficulty, 100, null, null, 1, Instant.parse("2025-01-01T00:00:00Z"), changedAt);
        CarryDifficultyHistory newPrice = new CarryDifficultyHistory(
                difficulty, 200, null, null, 1, changedAt, changedAt.plusSeconds(100));
        when(first.getCarryDifficulty()).thenReturn(difficulty);
        when(second.getCarryDifficulty()).thenReturn(difficulty);
        when(current.getCarryDifficulty()).thenReturn(difficulty);
        when(first.getTime()).thenReturn(changedAt.minusSeconds(1));
        when(second.getTime()).thenReturn(changedAt);
        when(current.getTime()).thenReturn(changedAt.plusSeconds(101));
        when(first.calculateTotalPrice(oldPrice)).thenReturn(100L);
        when(second.calculateTotalPrice(newPrice)).thenReturn(200L);
        when(current.calculateTotalPrice()).thenReturn(300L);
        when(difficultyService.loadPriceHistory(List.of(difficulty))).thenReturn(List.of(newPrice, oldPrice));

        ToLongFunction<Carry> calculator = service.historicalPriceCalculator(List.of(first, second, current));

        assertEquals(100, calculator.applyAsLong(first));
        assertEquals(200, calculator.applyAsLong(second));
        assertEquals(300, calculator.applyAsLong(current));
        verify(difficultyService, times(1)).loadPriceHistory(List.of(difficulty));
    }

    @Test
    void carryWithoutTimestampUsesCurrentPrice() {
        CarryDifficultyService difficultyService = mock(CarryDifficultyService.class);
        CarryService service = new CarryService(mock(CarryRepository.class), difficultyService);
        CarryDifficulty difficulty = mock(CarryDifficulty.class);
        Carry carry = mock(Carry.class);
        CarryDifficultyHistory history = new CarryDifficultyHistory(difficulty, 100, null, null, 1,
                Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-02-01T00:00:00Z"));
        when(carry.getCarryDifficulty()).thenReturn(difficulty);
        when(carry.getTime()).thenReturn(null);
        when(carry.calculateTotalPrice()).thenReturn(250L);
        when(difficultyService.loadPriceHistory(List.of(difficulty))).thenReturn(List.of(history));

        ToLongFunction<Carry> calculator = service.historicalPriceCalculator(List.of(carry));

        assertEquals(250, calculator.applyAsLong(carry));
        verify(carry, never()).calculateTotalPrice(history);
    }

    @Test
    void emptyCarryListLoadsEmptyHistoryOnceAndProducesCurrentPriceFallback() {
        CarryDifficultyService difficultyService = mock(CarryDifficultyService.class);
        CarryService service = new CarryService(mock(CarryRepository.class), difficultyService);
        when(difficultyService.loadPriceHistory(List.of())).thenReturn(List.of());
        Carry carry = mock(Carry.class);
        when(carry.calculateTotalPrice()).thenReturn(75L);

        ToLongFunction<Carry> calculator = service.historicalPriceCalculator(List.of());

        assertEquals(75, calculator.applyAsLong(carry));
        verify(difficultyService).loadPriceHistory(List.of());
    }
}
