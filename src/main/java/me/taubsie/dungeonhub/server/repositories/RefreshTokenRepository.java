package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository /*extends JpaRepository<RefreshToken, Long>*/ {
    Optional<RefreshToken> findByToken(String token);
}