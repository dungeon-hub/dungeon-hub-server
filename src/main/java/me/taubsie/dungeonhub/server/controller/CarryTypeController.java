package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.common.model.carry_type.CarryTypeCreationModel;
import me.taubsie.dungeonhub.common.model.carry_type.CarryTypeModel;
import me.taubsie.dungeonhub.common.model.carry_type.CarryTypeUpdateModel;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryTypeInitializeModel;
import me.taubsie.dungeonhub.server.service.CarryTypeService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/carry-type")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class CarryTypeController {
    private final DiscordServerService discordServerService;
    private final CarryTypeService carryTypeService;

    @Autowired
    public CarryTypeController(DiscordServerService discordServerService, CarryTypeService carryTypeService) {
        this.discordServerService = discordServerService;
        this.carryTypeService = carryTypeService;
    }

    @GetMapping("all")
    public List<CarryTypeModel> getAllCarryTypes(@PathVariable("server") long serverId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return carryTypeService.loadEntitiesByDiscordServer(discordServer).stream().map(CarryType::toModel).toList();
    }

    @GetMapping("{id}")
    public CarryTypeModel getById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return carryTypeService.loadEntityById(discordServer, id)
                .map(CarryType::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    public CarryTypeModel deleteById(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Optional<CarryType> carryType = carryTypeService.loadEntityById(discordServer, id);

        carryType.ifPresent(carryTypeService::delete);

        return carryType.map(CarryType::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public CarryTypeModel createNewCarryType(@PathVariable("server") long serverId,
                                             @RequestBody CarryTypeCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return carryTypeService.createEntity(new CarryTypeInitializeModel(discordServer)
                .fromCreationModel(creationModel)).toModel();
    }

    @PutMapping("{id}")
    public CarryTypeModel updateCarryType(@PathVariable("server") long serverId, @PathVariable long id,
                                          @RequestBody CarryTypeUpdateModel updateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return carryTypeService.update(carryType, updateModel).toModel();
    }
}