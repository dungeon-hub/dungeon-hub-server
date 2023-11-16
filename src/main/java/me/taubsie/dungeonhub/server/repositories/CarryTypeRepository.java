package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface CarryTypeRepository extends JpaRepository<CarryType, Long> {
    List<CarryType> findCarryTypesByServer(Server server);

    default Map<Long, CarryType> getCarryTypeMap() {
        return findAll().stream()
                .collect(Collectors.toMap(CarryType::getId, carryType -> carryType));
    }

    Optional<CarryType> findByIdentifier(String identifier);
}