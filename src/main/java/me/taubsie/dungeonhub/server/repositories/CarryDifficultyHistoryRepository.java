package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarryDifficultyHistoryRepository extends JpaRepository<CarryDifficultyHistory, Long> {
    List<CarryDifficultyHistory> findAllByCarryDifficulty_IdIn(Collection<Long> carryDifficultyIds);

    Optional<CarryDifficultyHistory> findFirstByCarryDifficultyOrderByDateToDesc(CarryDifficulty carryDifficulty);
}
