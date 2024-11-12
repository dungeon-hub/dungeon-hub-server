package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.model.CarryTierInitializeModel;
import me.taubsie.dungeonhub.server.service.CarryTierService;
import me.taubsie.dungeonhub.server.service.CarryTypeService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import net.dungeonhub.model.carry_tier.CarryTierCreationModel;
import net.dungeonhub.model.carry_tier.CarryTierModel;
import net.dungeonhub.model.carry_tier.CarryTierUpdateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/carry-type/{carry-type}/carry-tier")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class CarryTierController {
    private final DiscordServerService discordServerService;
    private final CarryTypeService carryTypeService;
    private final CarryTierService carryTierService;

    @Autowired
    public CarryTierController(DiscordServerService discordServerService, CarryTypeService carryTypeService,
                               CarryTierService carryTierService) {
        this.discordServerService = discordServerService;
        this.carryTypeService = carryTypeService;
        this.carryTierService = carryTierService;
    }

    @GetMapping("all")
    public List<CarryTierModel> getAllCarryTiers(@PathVariable("server") long serverId,
                                                 @PathVariable("carry-type") long carryTypeId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return carryTierService.loadEntitiesByCarryType(carryType)
                .stream().map(CarryTier::toModel)
                .toList();
    }

    @PostMapping
    public CarryTierModel createCarryTier(@PathVariable("server") long serverId,
                                          @PathVariable("carry-type") long carryTypeId,
                                          @RequestBody CarryTierCreationModel creationModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return carryTierService.create(new CarryTierInitializeModel(carryType).fromCreationModel(creationModel));
    }

    @PutMapping("{id}")
    public CarryTierModel updateCarryTier(@PathVariable("server") long serverId,
                                          @PathVariable("carry-type") long carryTypeId,
                                          @PathVariable long id,
                                          @RequestBody CarryTierUpdateModel updateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        CarryTier carryTier = carryTierService.loadEntityById(carryType, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return carryTierService.update(carryTier, updateModel).toModel();
    }

    @DeleteMapping("{id}")
    public CarryTierModel deleteCarryTier(@PathVariable("server") long serverId,
                                          @PathVariable("carry-type") long carryTypeId,
                                          @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Optional<CarryTier> carryTier = carryTierService.loadEntityById(carryType, id);

        carryTier.ifPresent(carryTierService::delete);

        return carryTier.map(CarryTier::toModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}