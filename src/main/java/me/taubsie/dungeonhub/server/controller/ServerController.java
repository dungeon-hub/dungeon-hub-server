package me.taubsie.dungeonhub.server.controller;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyModel;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierModel;
import me.taubsie.dungeonhub.common.model.score.ScoreModel;
import me.taubsie.dungeonhub.common.model.server.ServerModel;
import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryTier;
import me.taubsie.dungeonhub.server.entities.Score;
import me.taubsie.dungeonhub.server.entities.Server;
import me.taubsie.dungeonhub.server.service.CarryDifficultyService;
import me.taubsie.dungeonhub.server.service.CarryTierService;
import me.taubsie.dungeonhub.server.service.ScoreService;
import me.taubsie.dungeonhub.server.service.ServerService;
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
public class ServerController {
    private final ServerService serverService;
    private final ScoreService scoreService;
    private final CarryTierService carryTierService;
    private final CarryDifficultyService carryDifficultyService;

    @GetMapping("{server}")
    public ServerModel getServerById(@PathVariable("server") long id) {
        return serverService.getOrCreate(id).toModel();
    }

    @GetMapping("{server}/score/{id}")
    public List<ScoreModel> getScores(@PathVariable("server") long serverId, @PathVariable long id) {
        Server server = serverService.getOrCreate(serverId);

        return scoreService.getAllScores(id, server)
                .stream().map(Score::toModel)
                .toList();
    }

    @GetMapping("{server}/carry-tiers")
    public List<CarryTierModel> getAllCarryTiers(@PathVariable("server") long serverId) {
        Server server = serverService.getOrCreate(serverId);

        //TODO own service method
        return carryTierService.findAllEntities()
                .stream().filter(carryTier -> carryTier.getCarryType().getServer().equals(server))
                .map(CarryTier::toModel)
                .toList();
    }

    @GetMapping("{server}/carry-difficulties")
    public List<CarryDifficultyModel> getAllCarryDifficulties(@PathVariable("server") long serverId) {
        Server server = serverService.getOrCreate(serverId);

        //TODO own service method
        return carryDifficultyService.findAllEntities()
                .stream().filter(carryDifficulty -> carryDifficulty.getCarryTier().getCarryType().getServer().equals(server))
                .map(CarryDifficulty::toModel)
                .toList();
    }

    @GetMapping("{server}/category/{category}/carry-tier")
    public CarryTierModel getCarryTierFromCategory(@PathVariable("server") long serverId,
                                                   @PathVariable("category") long categoryId) {
        Server server = serverService.getOrCreate(serverId);

        return carryTierService.findByCategory(server, categoryId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .toModel();
    }

    @GetMapping("all")
    public Set<ServerModel> getAllServers() {
        return serverService.findAll();
    }
}