package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.Server;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<Server, Long> {
}