package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CntRequest;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CntRequestRepository extends JpaRepository<CntRequest, Long> {
    Optional<CntRequest> findByMessageId(long messageId);

    List<CntRequest> findByUser(DiscordUser user);
}