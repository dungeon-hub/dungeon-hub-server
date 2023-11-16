package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarryDifficultyRepository extends JpaRepository<CarryDifficulty, Long> {
    Optional<CarryDifficulty> findCarryDifficultyByIdentifier(String identifier);

    List<CarryDifficulty> findCarryDifficultiesByCarryTier(CarryTier carryTier);
}