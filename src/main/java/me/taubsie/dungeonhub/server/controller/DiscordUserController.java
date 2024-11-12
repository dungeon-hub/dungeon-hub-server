package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.model.DiscordUserInitializeModel;
import me.taubsie.dungeonhub.server.service.CarryService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.DiscordUserService;
import net.dungeonhub.model.discord_user.DiscordUserCreationModel;
import net.dungeonhub.model.discord_user.DiscordUserModel;
import net.dungeonhub.model.discord_user.DiscordUserUpdateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/discord-users")
@PreAuthorize("hasAnyRole('bot', 'admin')")
public class DiscordUserController {
    private final DiscordUserService discordUserService;
    private final CarryService carryService;
    private final DiscordServerService discordServerService;

    @Autowired
    public DiscordUserController(DiscordUserService discordUserService, CarryService carryService, DiscordServerService discordServerService) {
        this.discordUserService = discordUserService;
        this.carryService = carryService;
        this.discordServerService = discordServerService;
    }

    @GetMapping("all")
    public List<DiscordUserModel> getAllUsers() {
        return discordUserService.findAllEntities().stream().map(DiscordUser::toModel).toList();
    }

    @PreAuthorize("true")
    @GetMapping("count-linked")
    public long countLinkedUsers() {
        return discordUserService.countLinkedUsers();
    }

    @GetMapping("{id}")
    public DiscordUserModel getById(@PathVariable long id) {
        return discordUserService.loadEntityById(id)
                .map(DiscordUser::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("find")
    public DiscordUserModel findUser(@RequestParam(value = "uuid") String uuid) {
        UUID userUuid;

        try {
            userUuid = UUID.fromString(uuid);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return discordUserService.loadEntityByMinecraftId(userUuid)
                .map(DiscordUser::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("{id}/carries/{server}")
    public int getCarryCount(@PathVariable long id, @PathVariable("server") long serverId) {
        DiscordServer server = discordServerService.getOrCreate(serverId);
        DiscordUser carrier = discordUserService.loadEntityOrCreate(id);

        return carryService.countCarries(server, carrier);
    }

    @PostMapping
    public DiscordUserModel createNewUser(@RequestBody DiscordUserCreationModel creationModel) {
        return discordUserService.createEntity(new DiscordUserInitializeModel()
                .fromCreationModel(creationModel)).toModel();
    }

    @PutMapping("{id}")
    public DiscordUserModel updateUser(@PathVariable long id, @RequestBody DiscordUserUpdateModel updateModel) {
        DiscordUser discordUser = discordUserService.loadEntityById(id)
                .orElseGet(() -> new DiscordUser(id));

        return discordUserService.update(discordUser, updateModel).toModel();
    }
}