package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.PurgeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurgeTypeRepository extends JpaRepository<PurgeType, Long> {
    Optional<PurgeType> findByIdentifier(String identifier);

    List<PurgeType> findPurgeTypesByCarryType(CarryType carryType);
}