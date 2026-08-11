package me.taubsie.dungeonhub.server.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CarryDifficultyHistoryTest {
    private final CarryDifficulty difficulty = mock(CarryDifficulty.class);

    @Test
    void periodIsInclusiveAtStartAndExclusiveAtEnd() {
        Instant start = Instant.parse("2025-01-01T00:00:00Z");
        Instant end = Instant.parse("2025-02-01T00:00:00Z");
        CarryDifficultyHistory history = new CarryDifficultyHistory(difficulty, 100, null, null, 2, start, end);

        assertFalse(history.includes(start.minusNanos(1)));
        assertTrue(history.includes(start));
        assertTrue(history.includes(end.minusNanos(1)));
        assertFalse(history.includes(end));
    }

}
