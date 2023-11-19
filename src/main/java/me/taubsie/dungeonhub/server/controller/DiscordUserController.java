package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.common.model.discord_user.DiscordUserCreationModel;
import me.taubsie.dungeonhub.common.model.discord_user.DiscordUserModel;
import me.taubsie.dungeonhub.common.model.discord_user.DiscordUserUpdateModel;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.model.DiscordUserInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/discord-users/")
@PreAuthorize("hasAnyRole('bot', 'admin')")
public class DiscordUserController {
    private final DiscordUserService discordUserService;

    @Autowired
    public DiscordUserController(DiscordUserService discordUserService) {
        this.discordUserService = discordUserService;
    }

    @GetMapping("all")
    public List<DiscordUserModel> getAllUsers() {
        return discordUserService.findAllEntities().stream().map(DiscordUser::toModel).toList();
    }

    @GetMapping("{id}")
    public DiscordUserModel getById(@PathVariable long id) {
        return discordUserService.loadEntityById(id)
                .map(DiscordUser::toModel)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
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