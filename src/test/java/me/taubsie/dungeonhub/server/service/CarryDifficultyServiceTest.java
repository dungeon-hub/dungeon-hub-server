package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyBulkPrice;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistoryBulkPrice;
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
        CarryDifficulty difficulty = difficulty(100, 3);
        Instant before = Instant.now();

        service.updateEntity(difficulty, updateModel("{\"price\": 200}"));

        CarryDifficultyHistory history = capturedHistory();
        assertSame(difficulty, history.getCarryDifficulty());
        assertEquals(100, history.getPrice());
        assertTrue(history.getBulkPrices().isEmpty());
        assertEquals(3, history.getScore());
        assertEquals(HISTORY_START, history.getDateFrom());
        assertFalse(history.getDateTo().isBefore(before));
        assertFalse(history.getDateTo().isAfter(Instant.now()));
        assertEquals(200, difficulty.getPrice());
    }

    @Test
    void laterChangeStartsWhereThePreviousPeriodEnded() {
        CarryDifficulty difficulty = difficulty(100, 3, bulk(5, 450));
        Instant previousEnd = Instant.parse("2025-02-01T00:00:00Z");
        when(historyRepository.findFirstByCarryDifficultyOrderByDateToDesc(difficulty))
                .thenReturn(Optional.of(new CarryDifficultyHistory(difficulty, 50, List.of(), 1,
                        Instant.parse("2025-01-01T00:00:00Z"), previousEnd)));

        service.updateEntity(difficulty, updateModel("{\"bulkPrices\": [], \"score\": 4}"));

        CarryDifficultyHistory history = capturedHistory();
        assertEquals(previousEnd, history.getDateFrom());
        assertEquals(100, history.getPrice());
        assertEquals(List.of(new CarryDifficultyHistoryBulkPrice(5, 450)), history.getBulkPrices());
        assertEquals(3, history.getScore());
        assertTrue(difficulty.getBulkPrices().isEmpty());
        assertEquals(4, difficulty.getScore());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"price\": 101}", "{\"bulkPrices\": [{\"amount\": 5, \"price\": 80}]}",
            "{\"bulkPrices\": []}", "{\"bulkPrices\": [{\"amount\": 5, \"price\": 90}, {\"amount\": 10, \"price\": 80}]}",
            "{\"score\": 9}"})
    void changingATrackedValueCreatesHistory(String update) {
        service.updateEntity(difficulty(100, 3, bulk(5, 90)), updateModel(update));

        verify(historyRepository).save(any(CarryDifficultyHistory.class));
    }

    @Test
    void untrackedChangeDoesNotCreateHistory() {
        CarryDifficulty difficulty = difficulty(100, 3);

        service.updateEntity(difficulty, updateModel("{\"displayName\": \"New name\", \"priceName\": \"Coins\"}"));

        assertEquals("New name", difficulty.getDisplayName());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void settingTheCurrentValuesAgainDoesNotCreateHistory() {
        CarryDifficulty difficulty = difficulty(100, 3, bulk(5, 90));

        service.updateEntity(difficulty, updateModel(
                "{\"price\": 100, \"bulkPrices\": [{\"amount\": 5, \"price\": 90}], \"score\": 3}"));

        verifyNoInteractions(historyRepository);
    }

    @Test
    void updateSavesTheChangedDifficulty() {
        CarryDifficulty difficulty = difficulty(100, 3);
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

    private static CarryDifficultyBulkPrice bulk(int amount, int price) {
        return new CarryDifficultyBulkPrice(amount, price);
    }

    private static CarryDifficulty difficulty(int price, int score, CarryDifficultyBulkPrice... bulkPrices) {
        return new CarryDifficulty(1, "one", "One", null, null, List.of(bulkPrices), null, price, score, null);
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
