package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordRoleGroup;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.DiscordRoleGroupInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordRoleGroupService;
import me.taubsie.dungeonhub.server.service.DiscordRoleService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import net.dungeonhub.model.discord_role_group.DiscordRoleGroupCreationModel;
import net.dungeonhub.model.discord_role_group.DiscordRoleGroupModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/role-group")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class DiscordRoleGroupController {
    private final DiscordServerService discordServerService;
    private final DiscordRoleService discordRoleService;
    private final DiscordRoleGroupService discordRoleGroupService;

    @Autowired
    public DiscordRoleGroupController(DiscordServerService discordServerService, DiscordRoleService discordRoleService, DiscordRoleGroupService discordRoleGroupService) {
        this.discordServerService = discordServerService;
        this.discordRoleService = discordRoleService;
        this.discordRoleGroupService = discordRoleGroupService;
    }

    @GetMapping("all")
    public List<DiscordRoleGroupModel> getAllRoleGroups(@PathVariable("server") long serverId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return discordRoleGroupService.loadEntitiesByDiscordServer(discordServer).stream().map(DiscordRoleGroup::toModel).toList();
    }

    @GetMapping("{id}")
    public DiscordRoleGroupModel getById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return discordRoleGroupService.loadEntityById(discordServer, id)
                .map(DiscordRoleGroup::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    public DiscordRoleGroupModel deleteById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Optional<DiscordRoleGroup> discordRoleGroup = discordRoleGroupService.loadEntityById(discordServer, id);

        discordRoleGroup.ifPresent(discordRoleGroupService::delete);

        return discordRoleGroup.map(DiscordRoleGroup::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public DiscordRoleGroupModel createNewRoleGroup(@PathVariable("server") long serverId,
                                                    @RequestBody DiscordRoleGroupCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        DiscordRole discordRole = discordRoleService.loadOrCreate(discordServer, creationModel.getDiscordRole().getId());
        DiscordRole roleModel = discordRoleService.loadOrCreate(discordServer, creationModel.getRoleGroup().getId());

        if (!discordRole.getDiscordServer().equals(discordServer) || !roleModel.getDiscordServer().equals(discordServer)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return discordRoleGroupService.createEntity(new DiscordRoleGroupInitializeModel(discordRole, roleModel)
                .fromCreationModel(creationModel)).toModel();
    }
}