package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.Carry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarryRepository extends JpaRepository<Carry, Long> {
}
