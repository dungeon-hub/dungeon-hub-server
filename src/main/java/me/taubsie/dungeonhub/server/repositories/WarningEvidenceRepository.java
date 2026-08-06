package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.WarningEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarningEvidenceRepository extends JpaRepository<WarningEvidence, Long> {
}