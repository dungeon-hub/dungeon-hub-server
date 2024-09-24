package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CntRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CntRequestRepository extends JpaRepository<CntRequest, Long> {
    Optional<CntRequest> findByMessageId(long messageId);
}