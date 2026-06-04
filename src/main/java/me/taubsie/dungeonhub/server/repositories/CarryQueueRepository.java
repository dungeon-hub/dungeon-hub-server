package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CarryQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarryQueueRepository extends JpaRepository<CarryQueue, Long> {
    List<CarryQueue> findCarryQueuesByNotified(boolean notified);
}