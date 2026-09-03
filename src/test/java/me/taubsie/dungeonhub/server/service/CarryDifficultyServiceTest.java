package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyHistoryRepository;
import me.taubsie.dungeonhub.server.repositories.CarryDifficultyRepository;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyUpdateModel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarryDifficultyServiceTest {
    @Test
    void firstPriceUpdateStoresTheReplacedValuesAsAClosedPeriod() {
        CarryDifficultyRepository difficultyRepository = mock(CarryDifficultyRepository.class);
        CarryDifficultyHistoryRepository historyRepository = mock(CarryDifficultyHistoryRepository.class);
        CarryDifficultyService service = new CarryDifficultyService(difficultyRepository, historyRepository);
        CarryDifficulty difficulty = new CarryDifficulty(1, "one", "One", null, null,
                null, null, null, 100, 3, null);
        CarryDifficultyUpdateModel update = mock(CarryDifficultyUpdateModel.class);
        when(update.getPrice()).thenReturn(200);

        service.updateEntity(difficulty, update);

        ArgumentCaptor<CarryDifficultyHistory> captor = ArgumentCaptor.forClass(CarryDifficultyHistory.class);
        verify(historyRepository).save(captor.capture());
        CarryDifficultyHistory replacedPeriod = captor.getValue();
        assertEquals(100, replacedPeriod.getPrice());
        assertEquals(Instant.parse("1970-01-01T00:00:01Z"), replacedPeriod.getDateFrom());
        assertNotNull(replacedPeriod.getDateTo());
    }

    @Test
    void nonPricingUpdateDoesNotCreateHistory() {
        CarryDifficultyRepository difficultyRepository = mock(CarryDifficultyRepository.class);
        CarryDifficultyHistoryRepository historyRepository = mock(CarryDifficultyHistoryRepository.class);
        CarryDifficultyService service = new CarryDifficultyService(difficultyRepository, historyRepository);
        CarryDifficulty difficulty = new CarryDifficulty(1, "one", "One", null, null,
                null, null, null, 100, 3, null);
        CarryDifficultyUpdateModel update = mock(CarryDifficultyUpdateModel.class);
        when(update.getDisplayName()).thenReturn("New name");

        service.updateEntity(difficulty, update);

        verifyNoInteractions(historyRepository);
    }

    @Test
    void subsequentPriceUpdateStartsAtPreviousPeriodsEnd() {
        CarryDifficultyRepository difficultyRepository = mock(CarryDifficultyRepository.class);
        CarryDifficultyHistoryRepository historyRepository = mock(CarryDifficultyHistoryRepository.class);
        CarryDifficultyService service = new CarryDifficultyService(difficultyRepository, historyRepository);
        CarryDifficulty difficulty = new CarryDifficulty(1, "one", "One", null, null,
                450, 5, null, 100, 3, null);
        Instant previousEnd = Instant.parse("2025-02-01T00:00:00Z");
        CarryDifficultyHistory previous = new CarryDifficultyHistory(difficulty, 50, null, null, 1,
                Instant.parse("2025-01-01T00:00:00Z"), previousEnd);
        CarryDifficultyUpdateModel update = mock(CarryDifficultyUpdateModel.class);
        when(update.getResetBulkPrice()).thenReturn(true);
        when(update.getScore()).thenReturn(4);
        when(historyRepository.findFirstByCarryDifficultyOrderByDateToDesc(difficulty))
                .thenReturn(Optional.of(previous));

        service.updateEntity(difficulty, update);

        ArgumentCaptor<CarryDifficultyHistory> captor = ArgumentCaptor.forClass(CarryDifficultyHistory.class);
        verify(historyRepository).save(captor.capture());
        CarryDifficultyHistory replacedPeriod = captor.getValue();
        assertEquals(previousEnd, replacedPeriod.getDateFrom());
        assertEquals(100, replacedPeriod.getPrice());
        assertEquals(450, replacedPeriod.getBulkPrice());
        assertEquals(5, replacedPeriod.getBulkAmount());
        assertEquals(3, replacedPeriod.getScore());
        assertNull(difficulty.getBulkPrice());
        assertEquals(4, difficulty.getScore());
    }

    @Test
    void emptyHistoryRequestDoesNotAccessRepository() {
        CarryDifficultyRepository difficultyRepository = mock(CarryDifficultyRepository.class);
        CarryDifficultyHistoryRepository historyRepository = mock(CarryDifficultyHistoryRepository.class);
        CarryDifficultyService service = new CarryDifficultyService(difficultyRepository, historyRepository);

        assertEquals(List.of(), service.loadPriceHistory(List.of()));
        verifyNoInteractions(historyRepository);
    }
}
