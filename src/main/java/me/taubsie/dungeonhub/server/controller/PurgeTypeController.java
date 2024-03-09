package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.common.model.purge_type.PurgeTypeModel;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.PurgeType;
import me.taubsie.dungeonhub.server.service.CarryTypeService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.PurgeTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/server/{server}/carry-type/{carry-type}/purge-type")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class PurgeTypeController {
    private final DiscordServerService discordServerService;
    private final CarryTypeService carryTypeService;
    private final PurgeTypeService purgeTypeService;

    @Autowired
    public PurgeTypeController(DiscordServerService discordServerService, CarryTypeService carryTypeService, PurgeTypeService purgeTypeService) {
        this.discordServerService = discordServerService;
        this.carryTypeService = carryTypeService;
        this.purgeTypeService = purgeTypeService;
    }

    @GetMapping("{id}")
    public PurgeTypeModel getById(@PathVariable("server") long serverId, @PathVariable("carry-type") long carryTypeId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return carryTypeService.loadEntityById(discordServer, carryTypeId)
                .flatMap(carryType -> purgeTypeService.loadEntityById(carryType, id))
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .toModel();
    }

    @GetMapping("all")
    public List<PurgeTypeModel> getPurgeTypes(@PathVariable("server") long serverId, @PathVariable("carry-type") long carryTypeId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        return purgeTypeService.loadEntitiesByCarryType(carryType)
                .stream().map(PurgeType::toModel)
                .toList();
    }
}