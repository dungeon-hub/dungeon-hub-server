package me.taubsie.dungeonhub.server.entities;

import net.dungeonhub.model.carry_difficulty.BulkPriceModel;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CarryDifficultyHistoryTest {
    private static final Instant START = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2025-02-01T00:00:00Z");

    @Test
    void periodIsInclusiveAtStartAndExclusiveAtEnd() {
        CarryDifficultyHistory history = history(100);

        assertFalse(history.includes(START.minusNanos(1)));
        assertTrue(history.includes(START));
        assertTrue(history.includes(END.minusNanos(1)));
        assertFalse(history.includes(END));
    }

    @Test
    void totalPriceUsesRegularPriceWithoutBulkPricing() {
        assertEquals(300, history(100).calculateTotalPrice(3));
    }

    @Test
    void totalPriceUsesBulkPriceFromBulkAmountOnwards() {
        CarryDifficultyHistory history = history(100, new CarryDifficultyHistoryBulkPrice(5, 80));

        assertEquals(400, history.calculateTotalPrice(4));
        assertEquals(400, history.calculateTotalPrice(5));
        assertEquals(480, history.calculateTotalPrice(6));
    }

    @Test
    void totalPriceUsesTheBestMatchingOfMultipleBulkPrices() {
        CarryDifficultyHistory history = history(100, new CarryDifficultyHistoryBulkPrice(10, 70),
                new CarryDifficultyHistoryBulkPrice(5, 80));

        assertEquals(400, history.calculateTotalPrice(4));
        assertEquals(480, history.calculateTotalPrice(6));
        assertEquals(700, history.calculateTotalPrice(10));
        assertEquals(770, history.calculateTotalPrice(11));
        assertEquals(CarryDifficultyModel.Companion.calculateTotalPrice(11,
                List.of(new BulkPriceModel(5, 80), new BulkPriceModel(10, 70)), 100), history.calculateTotalPrice(11));
    }

    @Test
    void totalPriceMatchesCurrentPricingOfTheDifficulty() {
        CarryDifficulty difficulty = new CarryDifficulty(1, "one", "One", null, null,
                List.of(new CarryDifficultyBulkPrice(5, 80), new CarryDifficultyBulkPrice(10, 70)), null, 100, 1, null);
        CarryDifficultyHistory history = history(100, new CarryDifficultyHistoryBulkPrice(5, 80),
                new CarryDifficultyHistoryBulkPrice(10, 70));

        for (int amount = 1; amount <= 11; amount++) {
            assertEquals(difficulty.calculateTotalPrice(amount), history.calculateTotalPrice(amount),
                    "amount " + amount);
        }
    }

    private static CarryDifficultyHistory history(int price, CarryDifficultyHistoryBulkPrice... bulkPrices) {
        return new CarryDifficultyHistory(null, price, List.of(bulkPrices), 1, START, END);
    }
}
