package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.StaticMessage;
import net.dungeonhub.enums.StaticMessageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaticMessageRepository extends JpaRepository<StaticMessage, Long> {
    List<StaticMessage> findAllByServer(DiscordServer server);

    List<StaticMessage> findAllByServerAndChannelId(DiscordServer server, Long channelId);

    List<StaticMessage> findAllByServerAndStaticMessageType(DiscordServer server, StaticMessageType staticMessageType);

    List<StaticMessage> findAllByStaticMessageType(StaticMessageType staticMessageType);
}
