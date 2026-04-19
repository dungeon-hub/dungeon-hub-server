package me.taubsie.dungeonhub.server.controller;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.StaticMessage;
import me.taubsie.dungeonhub.server.model.StaticMessageInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.StaticMessageService;
import net.dungeonhub.enums.StaticMessageType;
import net.dungeonhub.model.static_message.StaticMessageCreationModel;
import net.dungeonhub.model.static_message.StaticMessageModel;
import net.dungeonhub.model.static_message.StaticMessageUpdateModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/server/{server}/static-message")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class StaticMessageController {
    private final DiscordServerService discordServerService;
    private final StaticMessageService staticMessageService;

    @GetMapping("{id}")
    public StaticMessageModel getById(
            @PathVariable("server") long serverId,
            @PathVariable long id
    ) {
        DiscordServer server = discordServerService.loadEntityById(serverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Server not found"));

        return staticMessageService.loadEntityById(id)
                .filter(entity -> entity.getServer().getId() == server.getId())
                .map(StaticMessage::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("find")
    public List<StaticMessageModel> findStaticMessages(
            @PathVariable("server") long serverId,
            @RequestParam(value = "staticMessageType", required = false) StaticMessageType staticMessageType,
            @RequestParam(value = "channelId", required = false) Long channelId,
            @RequestParam(value = "messageId", required = false) Long messageId
    ) {
        DiscordServer server = discordServerService.loadEntityById(serverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Server not found"));

        if(staticMessageType == null && channelId == null) {
            return staticMessageService.loadEntitiesByDiscordServer(server).stream()
                    .filter(staticMessage -> messageId == null || Objects.equals(staticMessage.getMessageId(), messageId))
                    .map(StaticMessage::toModel)
                    .toList();
        } else if(channelId != null) {
            Stream<StaticMessage> staticMessages = staticMessageService.loadEntitiesByDiscordServerAndChannel(server, channelId).stream();

            if(staticMessageType != null) {
                return staticMessages
                        .filter(staticMessage -> staticMessage.getStaticMessageType() == staticMessageType)
                        .filter(staticMessage -> messageId == null || Objects.equals(staticMessage.getMessageId(), messageId))
                        .map(StaticMessage::toModel)
                        .toList();
            } else {
                return staticMessages
                        .filter(staticMessage -> messageId == null || Objects.equals(staticMessage.getMessageId(), messageId))
                        .map(StaticMessage::toModel)
                        .toList();
            }
        } else {
            return staticMessageService.loadEntitiesByDiscordServerAndMessageType(server, staticMessageType).stream()
                    .filter(staticMessage -> messageId == null || Objects.equals(staticMessage.getMessageId(), messageId))
                    .map(StaticMessage::toModel)
                    .toList();
        }
    }

    @PostMapping
    public StaticMessageModel createStaticMessage(
            @PathVariable("server") long serverId,
            @RequestBody StaticMessageCreationModel creationModel
    ) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return staticMessageService.createEntity(new StaticMessageInitializeModel(discordServer)
                .fromCreationModel(creationModel)).toModel();
    }

    @PutMapping("{id}")
    public StaticMessageModel updateStaticMessage(
            @PathVariable("server") long serverId,
            @PathVariable long id,
            @RequestBody StaticMessageUpdateModel updateModel
    ) {
        DiscordServer server = discordServerService.loadEntityById(serverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Server not found"));

        StaticMessage staticMessage = staticMessageService.loadEntityById(server, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return staticMessageService.update(staticMessage, updateModel).toModel();
    }

    @DeleteMapping("{id}")
    public StaticMessageModel deleteStaticMessage(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Optional<StaticMessage> staticMessage = staticMessageService.loadEntityById(discordServer, id);

        staticMessage.ifPresent(staticMessageService::delete);

        return staticMessage.map(StaticMessage::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
