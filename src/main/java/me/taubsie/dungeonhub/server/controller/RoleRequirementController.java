package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.taubsie.dungeonhub.server.entities.DiscordRole;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.RoleRequirement;
import me.taubsie.dungeonhub.server.model.RoleRequirementInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordRoleService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.RoleRequirementService;
import net.dungeonhub.model.role_requirement.RoleRequirementCreationModel;
import net.dungeonhub.model.role_requirement.RoleRequirementModel;
import net.dungeonhub.model.role_requirement.RoleRequirementUpdateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/role-requirement")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@Tag(name = "Role-Requirement")
public class RoleRequirementController {
    private final RoleRequirementService roleRequirementService;
    private final DiscordServerService discordServerService;
    private final DiscordRoleService discordRoleService;

    @Autowired
    public RoleRequirementController(RoleRequirementService roleRequirementService, DiscordServerService discordServerService, DiscordRoleService discordRoleService) {
        this.roleRequirementService = roleRequirementService;
        this.discordServerService = discordServerService;
        this.discordRoleService = discordRoleService;
    }

    @GetMapping("{id}")
    public RoleRequirementModel getById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return roleRequirementService.loadEntityById(discordServer, id)
                .map(RoleRequirement::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("all")
    public List<RoleRequirementModel> getAllRoleRequirements(@PathVariable("server") long serverId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return roleRequirementService.loadEntitiesByDiscordServer(discordServer).stream().map(RoleRequirement::toModel).toList();
    }

    @DeleteMapping("{id}")
    public RoleRequirementModel deleteById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Optional<RoleRequirement> roleRequirement = roleRequirementService.loadEntityById(discordServer, id);

        roleRequirement.ifPresent(roleRequirementService::delete);

        return roleRequirement.map(RoleRequirement::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public RoleRequirementModel createNewRoleRequirement(@PathVariable("server") long serverId,
                                             @RequestBody RoleRequirementCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        DiscordRole discordRole = discordRoleService.loadOrCreate(discordServer, creationModel.getDiscordRole());

        return roleRequirementService.createEntity(new RoleRequirementInitializeModel(discordRole)
                .fromCreationModel(creationModel)).toModel();
    }

    @PutMapping("{id}")
    public RoleRequirementModel updateRoleRequirement(@PathVariable("server") long serverId, @PathVariable long id,
                                          @RequestBody RoleRequirementUpdateModel updateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        RoleRequirement roleRequirement = roleRequirementService.loadEntityById(discordServer, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return roleRequirementService.update(roleRequirement, updateModel).toModel();
    }
}