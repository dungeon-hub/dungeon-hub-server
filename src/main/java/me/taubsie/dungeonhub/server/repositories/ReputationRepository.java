package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.Reputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReputationRepository extends JpaRepository<Reputation, Long> {
}