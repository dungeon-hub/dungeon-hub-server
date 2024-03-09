package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyModel;
import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyUpdateModel;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.service.CarryDifficultyService;
import me.taubsie.dungeonhub.server.service.CarryTierService;
import me.taubsie.dungeonhub.server.service.CarryTypeService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/server/{server}/carry-type/{carry-type}/carry-tier/{carry-tier}/carry-difficulty")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
public class CarryDifficultyController {
    private final DiscordServerService discordServerService;
    private final CarryTypeService carryTypeService;
    private final CarryTierService carryTierService;
    private final CarryDifficultyService carryDifficultyService;

    @Autowired
    public CarryDifficultyController(DiscordServerService discordServerService, CarryTypeService carryTypeService,
                                     CarryTierService carryTierService, CarryDifficultyService carryDifficultyService) {
        this.discordServerService = discordServerService;
        this.carryTypeService = carryTypeService;
        this.carryTierService = carryTierService;
        this.carryDifficultyService = carryDifficultyService;
    }

    private CarryTier getFromArguments(long serverId, long carryTypeId, long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return carryTierService.loadEntityById(carryType, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private CarryDifficulty getFromArguments(long serverId, long carryTypeId, long carryTierId, long id) {
        CarryTier carryTier = getFromArguments(serverId, carryTypeId, carryTierId);

        return carryDifficultyService.loadEntityById(carryTier, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("all")
    public List<CarryDifficultyModel> getAllCarryDifficulties(@PathVariable("server") long serverId, @PathVariable(
            "carry-type") long carryTypeId, @PathVariable("carry-tier") long carryTierId) {
        CarryTier carryTier = getFromArguments(serverId, carryTypeId, carryTierId);

        return carryDifficultyService.findByCarryTier(carryTier)
                .stream().map(CarryDifficulty::toModel)
                .toList();
    }

    @GetMapping("{id}")
    public CarryDifficultyModel getCarryDifficulty(@PathVariable("server") long serverId, @PathVariable("carry" +
            "-type") long carryTypeId, @PathVariable("carry-tier") long carryTierId, @PathVariable long id) {
        return getFromArguments(serverId, carryTypeId, carryTierId, id).toModel();
    }


    @PutMapping("{id}")
    public CarryDifficultyModel updateCarryDifficulty(@PathVariable("server") long serverId, @PathVariable("carry" +
            "-type") long carryTypeId, @PathVariable("carry-tier") long carryTierId, @PathVariable long id,
                                                      @RequestBody CarryDifficultyUpdateModel updateModel) {
        CarryDifficulty carryDifficulty = getFromArguments(serverId, carryTypeId, carryTierId, id);

        return carryDifficultyService.update(carryDifficulty, updateModel).toModel();
    }
}