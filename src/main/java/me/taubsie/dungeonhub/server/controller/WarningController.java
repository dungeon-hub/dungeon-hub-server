package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.model.warning.DetailedWarningModel;
import me.taubsie.dungeonhub.common.model.warning.WarningCreationModel;
import me.taubsie.dungeonhub.common.model.warning.WarningEvidenceCreationModel;
import me.taubsie.dungeonhub.common.model.warning.WarningModel;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Warning;
import me.taubsie.dungeonhub.server.model.WarningEvidenceInitializeModel;
import me.taubsie.dungeonhub.server.model.WarningInitializeModel;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.DiscordUserService;
import me.taubsie.dungeonhub.server.service.WarningEvidenceService;
import me.taubsie.dungeonhub.server.service.WarningService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/server/{server}/warns")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@Tag(name = "Warns")
public class WarningController {
    private final WarningService warningService;
    private final DiscordServerService discordServerService;
    private final DiscordUserService discordUserService;
    private final WarningEvidenceService warningEvidenceService;

    @GetMapping("all")
    public List<DetailedWarningModel> getAllWarningsForUser(@PathVariable long server, @RequestParam long user) {
        return warningService.findAllWarningsForUser(server, user).stream()
                .map(Warning::toDetailedModel)
                .toList();
    }

    @GetMapping("active")
    public List<DetailedWarningModel> getAllActiveWarningsForUser(@PathVariable long server, @RequestParam long user) {
        return warningService.findAllActiveWarningsForUser(server, user).stream()
                .map(Warning::toDetailedModel)
                .toList();
    }

    @GetMapping("{id}")
    public DetailedWarningModel getWarningById(@PathVariable long server, @PathVariable long id) {
        return warningService.loadEntityById(id)
                .filter(warningModel -> warningModel.getServer().getId() == server)
                .map(Warning::toDetailedModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    public DetailedWarningModel deactivateWarning(@PathVariable long server, @PathVariable long id) {
        Warning warning = warningService.loadEntityById(id)
                .filter(w -> w.getServer().getId() == server)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return warningService.deactivateWarning(warning).toDetailedModel();
    }

    @PostMapping
    public WarningModel addWarning(@PathVariable long server, @RequestBody WarningCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(server);
        DiscordUser user = discordUserService.loadEntityOrCreate(creationModel.getUser());
        DiscordUser striker = discordUserService.loadEntityOrCreate(creationModel.getStriker());

        WarningInitializeModel initializeModel = new WarningInitializeModel(discordServer, user, striker).fromCreationModel(creationModel);

        return warningService.create(initializeModel);
    }

    @PutMapping("{id}/evidence")
    public DetailedWarningModel addEvidence(@PathVariable long server, @PathVariable long id, @RequestBody WarningEvidenceCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(server);
        Warning warning = warningService.loadEntityById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if(warning.getServer().getId() != discordServer.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        DiscordUser submitter = discordUserService.loadEntityOrCreate(creationModel.getSubmitter());

        WarningEvidenceInitializeModel initializeModel = new WarningEvidenceInitializeModel(warning, submitter).fromCreationModel(creationModel);

        warningEvidenceService.create(initializeModel);

        return warningService.loadEntityById(warning.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.EXPECTATION_FAILED))
                .toDetailedModel();
    }
}