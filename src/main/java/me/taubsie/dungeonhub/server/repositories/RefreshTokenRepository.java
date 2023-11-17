package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(UUID token);
}