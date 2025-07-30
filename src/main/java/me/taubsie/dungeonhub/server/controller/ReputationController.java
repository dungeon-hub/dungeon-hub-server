package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Reputation;
import me.taubsie.dungeonhub.server.model.ReputationInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.DiscordUserService;
import me.taubsie.dungeonhub.server.service.ReputationService;
import net.dungeonhub.model.reputation.ReputationCreationModel;
import net.dungeonhub.model.reputation.ReputationModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/discord-user/{discord-user}/reputation")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class ReputationController {
    private final ReputationService reputationService;
    private final DiscordUserService discordUserService;
    private final DiscordServerService discordServerService;

    public ReputationController(ReputationService reputationService, DiscordUserService discordUserService, DiscordServerService discordServerService) {
        this.reputationService = reputationService;
        this.discordUserService = discordUserService;
        this.discordServerService = discordServerService;
    }

    @GetMapping("all")
    public List<ReputationModel> getAllReputations(@PathVariable("server") long serverId, @PathVariable("discord-user") long discordUserId) {
        Optional<DiscordServer> discordServer = discordServerService.loadEntityById(serverId);

        Optional<DiscordUser> discordUser = discordUserService.loadEntityById(discordUserId);

        if (discordServer.isEmpty() || discordUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return reputationService.getAllReputations(discordServer.get(), discordUser.get()).stream()
                .map(Reputation::toModel)
                .toList();
    }

    @GetMapping("calculate")
    public long calculateReputation(@PathVariable("server") long serverId, @PathVariable("discord-user") long discordUserId) {
        Optional<DiscordServer> discordServer = discordServerService.loadEntityById(serverId);

        Optional<DiscordUser> discordUser = discordUserService.loadEntityById(discordUserId);

        if (discordServer.isEmpty() || discordUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return Optional.ofNullable(reputationService.calculateReputation(discordServer.get(), discordUser.get()))
                .orElse(0L);
    }

    @PostMapping
    public ReputationModel addReputation(@PathVariable("server") long serverId, @PathVariable("discord-user") long discordUserId, @RequestBody ReputationCreationModel creationModel) {
        if (discordUserId != creationModel.getUser()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        DiscordUser discordUser = discordUserService.loadEntityOrCreate(discordUserId);

        DiscordUser reputor = discordUserService.loadEntityOrCreate(creationModel.getReputor());

        return reputationService.createEntity(new ReputationInitializeModel(discordServer, discordUser, reputor)
                .fromCreationModel(creationModel)).toModel();
    }
}