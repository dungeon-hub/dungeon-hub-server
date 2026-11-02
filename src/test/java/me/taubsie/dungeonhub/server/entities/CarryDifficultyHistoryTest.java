package me.taubsie.dungeonhub.server.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CarryDifficultyHistoryTest {
    private static final Instant START = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2025-02-01T00:00:00Z");

    @Test
    void periodIsInclusiveAtStartAndExclusiveAtEnd() {
        CarryDifficultyHistory history = history(100, null, null);

        assertFalse(history.includes(START.minusNanos(1)));
        assertTrue(history.includes(START));
        assertTrue(history.includes(END.minusNanos(1)));
        assertFalse(history.includes(END));
    }

    @Test
    void totalPriceUsesRegularPriceWithoutBulkPricing() {
        assertEquals(300, history(100, null, null).calculateTotalPrice(3));
    }

    @Test
    void totalPriceUsesBulkPriceFromBulkAmountOnwards() {
        CarryDifficultyHistory history = history(100, 80, 5);

        assertEquals(400, history.calculateTotalPrice(4));
        assertEquals(400, history.calculateTotalPrice(5));
        assertEquals(480, history.calculateTotalPrice(6));
    }

    @Test
    void totalPriceMatchesCurrentPricingOfTheDifficulty() {
        CarryDifficulty difficulty = new CarryDifficulty(1, "one", "One", null, null, 80, 5, null, 100, 1, null);

        for (int amount = 1; amount <= 10; amount++) {
            assertEquals(difficulty.calculateTotalPrice(amount), history(100, 80, 5).calculateTotalPrice(amount));
        }
    }

    private static CarryDifficultyHistory history(int price, Integer bulkPrice, Integer bulkAmount) {
        return new CarryDifficultyHistory(null, price, bulkPrice, bulkAmount, 1, START, END);
    }
}
