package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.DiscordChannel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordChannelInitializeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscordChannelRepository extends JpaRepository<DiscordChannel, Long> {
    List<DiscordChannel> findDiscordChannelsByDiscordServer(DiscordServer discordServer);

    default DiscordChannel loadEntityOrCreate(DiscordServer discordServer, long id) {
        return findById(id)
                .filter(discordChannel -> discordChannel.getDiscordServer().getId() == discordServer.getId())
                .orElseGet(() -> save(new DiscordChannelInitializeModel(discordServer, id, null, false).toEntity()));
    }
}