package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.common.enums.WarningType;
import me.taubsie.dungeonhub.server.entities.Warning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarningRepository extends JpaRepository<Warning, Long> {
    List<Warning> findAllByServer_IdAndUser_Id(long serverId, long userId);

    List<Warning> findAllByServer_IdAndUser_IdAndActive(long serverId, long userId, boolean active);

    List<Warning> findAllByActiveAndWarningType(boolean active, WarningType warningType);
}