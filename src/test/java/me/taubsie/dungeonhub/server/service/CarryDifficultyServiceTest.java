package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyHistoryRepository;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyUpdateModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarryDifficultyServiceTest {
    private static final Instant HISTORY_START = Instant.parse("1970-01-01T00:00:01Z");

    private CarryDifficultyRepository difficultyRepository;
    private CarryDifficultyHistoryRepository historyRepository;
    private CarryDifficultyService service;

    @BeforeEach
    void setUp() {
        difficultyRepository = mock(CarryDifficultyRepository.class);
        historyRepository = mock(CarryDifficultyHistoryRepository.class);
        service = new CarryDifficultyService(difficultyRepository, historyRepository);
    }

    @Test
    void firstPriceChangeStoresReplacedValuesFromHistoryStartUntilNow() {
        CarryDifficulty difficulty = difficulty(null, null, 100, 3);
        Instant before = Instant.now();

        service.updateEntity(difficulty, updateModel("{\"price\": 200}"));

        CarryDifficultyHistory history = capturedHistory();
        assertSame(difficulty, history.getCarryDifficulty());
        assertEquals(100, history.getPrice());
        assertNull(history.getBulkPrice());
        assertNull(history.getBulkAmount());
        assertEquals(3, history.getScore());
        assertEquals(HISTORY_START, history.getDateFrom());
        assertFalse(history.getDateTo().isBefore(before));
        assertFalse(history.getDateTo().isAfter(Instant.now()));
        assertEquals(200, difficulty.getPrice());
    }

    @Test
    void laterChangeStartsWhereThePreviousPeriodEnded() {
        CarryDifficulty difficulty = difficulty(450, 5, 100, 3);
        Instant previousEnd = Instant.parse("2025-02-01T00:00:00Z");
        when(historyRepository.findFirstByCarryDifficultyOrderByDateToDesc(difficulty))
                .thenReturn(Optional.of(new CarryDifficultyHistory(difficulty, 50, null, null, 1,
                        Instant.parse("2025-01-01T00:00:00Z"), previousEnd)));

        service.updateEntity(difficulty, updateModel("{\"resetBulkPrice\": true, \"score\": 4}"));

        CarryDifficultyHistory history = capturedHistory();
        assertEquals(previousEnd, history.getDateFrom());
        assertEquals(100, history.getPrice());
        assertEquals(450, history.getBulkPrice());
        assertEquals(5, history.getBulkAmount());
        assertEquals(3, history.getScore());
        assertNull(difficulty.getBulkPrice());
        assertEquals(4, difficulty.getScore());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"price\": 101}", "{\"bulkPrice\": 80}", "{\"resetBulkPrice\": true}",
            "{\"bulkAmount\": 10}", "{\"resetBulkAmount\": true}", "{\"score\": 9}"})
    void changingATrackedValueCreatesHistory(String update) {
        service.updateEntity(difficulty(90, 5, 100, 3), updateModel(update));

        verify(historyRepository).save(any(CarryDifficultyHistory.class));
    }

    @Test
    void untrackedChangeDoesNotCreateHistory() {
        CarryDifficulty difficulty = difficulty(null, null, 100, 3);

        service.updateEntity(difficulty, updateModel("{\"displayName\": \"New name\", \"priceName\": \"Coins\"}"));

        assertEquals("New name", difficulty.getDisplayName());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void settingTheCurrentValuesAgainDoesNotCreateHistory() {
        CarryDifficulty difficulty = difficulty(90, 5, 100, 3);

        service.updateEntity(difficulty, updateModel(
                "{\"price\": 100, \"bulkPrice\": 90, \"bulkAmount\": 5, \"score\": 3}"));

        verifyNoInteractions(historyRepository);
    }

    @Test
    void updateSavesTheChangedDifficulty() {
        CarryDifficulty difficulty = difficulty(null, null, 100, 3);
        when(difficultyRepository.save(difficulty)).thenReturn(difficulty);

        CarryDifficulty result = service.update(difficulty, updateModel("{\"price\": 200}"));

        assertSame(difficulty, result);
        verify(historyRepository).save(any(CarryDifficultyHistory.class));
        verify(difficultyRepository).save(difficulty);
    }

    @Test
    void loadingHistoryForNoDifficultiesSkipsTheQuery() {
        assertEquals(List.of(), service.loadPriceHistory(List.of()));

        verifyNoInteractions(historyRepository);
    }

    private static CarryDifficulty difficulty(Integer bulkPrice, Integer bulkAmount, int price, int score) {
        return new CarryDifficulty(1, "one", "One", null, null, bulkPrice, bulkAmount, null, price, score, null);
    }

    private static CarryDifficultyUpdateModel updateModel(String json) {
        return CarryDifficultyUpdateModel.Companion.fromJson(json);
    }

    private CarryDifficultyHistory capturedHistory() {
        ArgumentCaptor<CarryDifficultyHistory> captor = ArgumentCaptor.forClass(CarryDifficultyHistory.class);
        verify(historyRepository).save(captor.capture());
        return captor.getValue();
    }
}
