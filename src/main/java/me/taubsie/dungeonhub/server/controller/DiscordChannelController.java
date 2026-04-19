package me.taubsie.dungeonhub.server.controller;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordChannel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordChannelInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordChannelService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import net.dungeonhub.model.discord_channel.DiscordChannelCreationModel;
import net.dungeonhub.model.discord_channel.DiscordChannelModel;
import net.dungeonhub.model.discord_channel.DiscordChannelUpdateModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/server/{server}/channel")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@AllArgsConstructor
public class DiscordChannelController {
    private final DiscordChannelService discordChannelService;
    private final DiscordServerService discordServerService;

    @GetMapping("all")
    public List<DiscordChannelModel> getAllChannels(@PathVariable("server") long serverId, @RequestParam(name = "also-show-deleted", required = false, defaultValue = "false") boolean alsoShowDeleted) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Stream<DiscordChannel> result = discordChannelService.loadEntitiesByDiscordServer(discordServer).stream();

        if(!alsoShowDeleted) {
            result = result.filter(discordChannel -> !discordChannel.isDeleted());
        }

        return result.map(DiscordChannel::toModel).toList();
    }

    @GetMapping("{id}")
    public DiscordChannelModel getById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return discordChannelService.loadEntityById(discordServer, id)
                .map(DiscordChannel::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    public DiscordChannelModel deleteById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Optional<DiscordChannel> discordChannel = discordChannelService.loadEntityById(discordServer, id);

        discordChannel.ifPresent(discordChannelService::delete);

        return discordChannel.map(DiscordChannel::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public DiscordChannelModel createNewChannel(@PathVariable("server") long serverId, @RequestBody DiscordChannelCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return discordChannelService.createEntity(new DiscordChannelInitializeModel(discordServer)
                .fromCreationModel(creationModel)).toModel();
    }

    @PutMapping("{id}")
    public DiscordChannelModel updateChannel(@PathVariable("server") long serverId, @PathVariable long id, @RequestBody DiscordChannelUpdateModel updateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        DiscordChannel discordChannel = discordChannelService.loadEntityById(discordServer, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return discordChannelService.update(discordChannel, updateModel).toModel();
    }
}