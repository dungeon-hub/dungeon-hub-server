package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierCreationModel;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierModel;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierUpdateModel;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.Server;
import me.taubsie.dungeonhub.server.model.CarryTierInitializeModel;
import me.taubsie.dungeonhub.server.service.CarryTierService;
import me.taubsie.dungeonhub.server.service.CarryTypeService;
import me.taubsie.dungeonhub.server.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/server/{server}/carry-type/{carry-type}/carry-tier")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class CarryTierController {
    private final ServerService serverService;
    private final CarryTypeService carryTypeService;
    private final CarryTierService carryTierService;

    @Autowired
    public CarryTierController(ServerService serverService, CarryTypeService carryTypeService,
                               CarryTierService carryTierService) {
        this.serverService = serverService;
        this.carryTypeService = carryTypeService;
        this.carryTierService = carryTierService;
    }

    @GetMapping("all")
    public List<CarryTierModel> getAllCarryTiers(@PathVariable("server") long serverId,
                                                 @PathVariable("carry-type") long carryTypeId) {
        Server server = serverService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(server, carryTypeId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        return carryTierService.loadEntitiesByCarryType(carryType)
                .stream().map(CarryTier::toModel)
                .toList();
    }

    @PostMapping
    public CarryTierModel createCarryTier(@PathVariable("server") long serverId,
                                          @PathVariable("carry-type") long carryTypeId,
                                          @RequestBody CarryTierCreationModel creationModel) {
        Server server = serverService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(server, carryTypeId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        return carryTierService.create(new CarryTierInitializeModel(carryType).fromCreationModel(creationModel));
    }

    @PutMapping("{id}")
    public CarryTierModel updateCarryTier(@PathVariable("server") long serverId,
                                          @PathVariable("carry-type") long carryTypeId,
                                          @PathVariable long id,
                                          @RequestBody CarryTierUpdateModel updateModel) {
        Server server = serverService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(server, carryTypeId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        CarryTier carryTier = carryTierService.loadEntityById(carryType, id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        return carryTierService.update(carryTier, updateModel).toModel();
    }
}