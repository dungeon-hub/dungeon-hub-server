package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.server.entities.CntRequest;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.model.CntRequestInitializeModel;
import me.taubsie.dungeonhub.server.service.CntRequestService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.DiscordUserService;
import net.dungeonhub.model.cnt_request.CntRequestCreationModel;
import net.dungeonhub.model.cnt_request.CntRequestModel;
import net.dungeonhub.model.cnt_request.CntRequestUpdateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/server/{server}/cnt-request")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class CntRequestController {
    private final CntRequestService cntRequestService;
    private final DiscordServerService discordServerService;
    private final DiscordUserService discordUserService;

    @Autowired
    public CntRequestController(CntRequestService cntRequestService, DiscordServerService discordServerService, DiscordUserService discordUserService) {
        this.cntRequestService = cntRequestService;
        this.discordServerService = discordServerService;
        this.discordUserService = discordUserService;
    }

    @GetMapping("find")
    public List<CntRequestModel> findCntRequest(
            @PathVariable("server") long serverId,
            @RequestParam(value = "message-id", required = false) Long messageId,
            @RequestParam(value = "user", required = false) Long userId
    ) {
        if (messageId != null) {
            return cntRequestService.findByMessageId(messageId)
                    .filter(cntRequest -> cntRequest.getDiscordServer().getId() == serverId)
                    .map(CntRequest::toModel)
                    .map(Collections::singletonList)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        if(userId != null) {
            DiscordUser user = discordUserService.loadEntityById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            return cntRequestService.findByUser(user).stream()
                    .map(CntRequest::toModel)
                    .toList();
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @PostMapping
    public CntRequestModel createNewCntRequest(@PathVariable("server") long serverId,
                                               @RequestBody CntRequestCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);
        DiscordUser user = discordUserService.loadEntityOrCreate(creationModel.getUser());
        DiscordUser claimer = creationModel.getClaimer() != null
                ? discordUserService.loadEntityOrCreate(creationModel.getClaimer())
                : null;

        return cntRequestService.createEntity(
                new CntRequestInitializeModel(discordServer, user, claimer)
                        .fromCreationModel(creationModel)
        ).toModel();
    }

    @PutMapping("{id}")
    public CntRequestModel updateCntRequest(@PathVariable("server") long serverId, @PathVariable long id,
                                            @RequestBody CntRequestUpdateModel updateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CntRequest cntRequest = cntRequestService.loadEntityById(discordServer, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return cntRequestService.update(cntRequest, updateModel).toModel();
    }
}