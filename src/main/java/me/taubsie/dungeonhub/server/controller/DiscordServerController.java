package me.taubsie.dungeonhub.server.controller;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyModel;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierModel;
import me.taubsie.dungeonhub.common.model.score.ScoreModel;
import me.taubsie.dungeonhub.common.model.server.DiscordServerModel;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Set;

@RestController
@EnableMethodSecurity
@RequestMapping("api/v1/server")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@AllArgsConstructor
public class DiscordServerController {
    private final DiscordServerService discordServerService;
    private final ScoreService scoreService;
    private final CarryTierService carryTierService;
    private final CarryDifficultyService carryDifficultyService;
    private final DiscordUserService discordUserService;

    @GetMapping("{server}")
    public DiscordServerModel getServerById(@PathVariable("server") long id) {
        return discordServerService.getOrCreate(id).toModel();
    }

    @GetMapping("{server}/score/{id}")
    public List<ScoreModel> getScores(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        DiscordUser carrier = discordUserService.loadEntityOrCreate(id);

        return scoreService.getAllScores(carrier, discordServer)
                .stream().map(Score::toModel)
                .toList();
    }

    @GetMapping("{server}/carry-tiers")
    public List<CarryTierModel> getAllCarryTiers(@PathVariable("server") long serverId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        //TODO own service method
        return carryTierService.findAllEntities()
                .stream().filter(carryTier -> carryTier.getCarryType().getDiscordServer().equals(discordServer))
                .map(CarryTier::toModel)
                .toList();
    }

    @GetMapping("{server}/carry-difficulties")
    public List<CarryDifficultyModel> getAllCarryDifficulties(@PathVariable("server") long serverId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        //TODO own service method
        return carryDifficultyService.findAllEntities()
                .stream().filter(carryDifficulty -> carryDifficulty.getCarryTier().getCarryType().getDiscordServer().equals(discordServer))
                .map(CarryDifficulty::toModel)
                .toList();
    }

    @GetMapping("{server}/category/{category}/carry-tier")
    public CarryTierModel getCarryTierFromCategory(@PathVariable("server") long serverId,
                                                   @PathVariable("category") long categoryId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return carryTierService.findByCategory(discordServer, categoryId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .toModel();
    }

    @GetMapping("all")
    public Set<DiscordServerModel> getAllServers() {
        return discordServerService.findAll();
    }
}