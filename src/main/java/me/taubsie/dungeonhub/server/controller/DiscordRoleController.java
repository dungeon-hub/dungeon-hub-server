package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.common.model.discord_role.DiscordRoleCreationModel;
import me.taubsie.dungeonhub.common.model.discord_role.DiscordRoleModel;
import me.taubsie.dungeonhub.common.model.discord_role.DiscordRoleUpdateModel;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordRoleInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordRoleService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/roles")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class DiscordRoleController {
    private final DiscordServerService discordServerService;
    private final DiscordRoleService discordRoleService;

    @Autowired
    public DiscordRoleController(DiscordServerService discordServerService, DiscordRoleService discordRoleService) {
        this.discordServerService = discordServerService;
        this.discordRoleService = discordRoleService;
    }

    @GetMapping("all")
    public List<DiscordRoleModel> getAllRoles(@PathVariable("server") long serverId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return discordRoleService.loadEntitiesByDiscordServer(discordServer).stream().map(DiscordRole::toModel).toList();
    }

    @GetMapping("{id}")
    public DiscordRoleModel getById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return discordRoleService.loadEntityById(discordServer, id)
                .map(DiscordRole::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    public DiscordRoleModel deleteById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Optional<DiscordRole> discordRole = discordRoleService.loadEntityById(discordServer, id);

        discordRole.ifPresent(discordRoleService::delete);

        return discordRole.map(DiscordRole::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public DiscordRoleModel createNewRole(@PathVariable("server") long serverId,
                                          @RequestBody DiscordRoleCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return discordRoleService.createEntity(new DiscordRoleInitializeModel(discordServer)
                .fromCreationModel(creationModel)).toModel();
    }

    @PutMapping("{id}")
    public DiscordRoleModel updateRole(@PathVariable("server") long serverId, @PathVariable long id,
                                       @RequestBody DiscordRoleUpdateModel updateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        DiscordRole discordRole = discordRoleService.loadEntityById(discordServer, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return discordRoleService.update(discordRole, updateModel).toModel();
    }
}