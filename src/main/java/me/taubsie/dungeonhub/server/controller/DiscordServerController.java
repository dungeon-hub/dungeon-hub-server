package me.taubsie.dungeonhub.server.controller;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.enums.ScoreType;
import me.taubsie.dungeonhub.common.model.carry_difficulty.CarryDifficultyModel;
import me.taubsie.dungeonhub.common.model.carry_tier.CarryTierModel;
import me.taubsie.dungeonhub.common.model.score.LeaderboardModel;
import me.taubsie.dungeonhub.common.model.score.ScoreModel;
import me.taubsie.dungeonhub.common.model.server.DiscordServerModel;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.service.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("api/v1/server")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@AllArgsConstructor
public class DiscordServerController {
    private final DiscordServerService discordServerService;
    private final ScoreService scoreService;
    private final CarryTypeService carryTypeService;
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

        //TODO maybe move the following code to service? service should return the models afterall imo
        List<ScoreModel> scores = new ArrayList<>(scoreService.getAllScores(carrier, discordServer)
                .stream().map(Score::toModel)
                .toList());

        for (CarryType carryType : carryTypeService.loadEntitiesByDiscordServer(discordServer)) {
            for (ScoreType scoreType : ScoreType.values()) {
                if (scores.stream()
                        .filter(scoreModel -> scoreModel.getScoreType() == scoreType)
                        .filter(scoreModel -> scoreModel.getCarryType().getId() == carryType.getId())
                        .findAny().isEmpty()) {
                    scores.add(new ScoreModel(carrier.toModel(), carryType.toModel(), scoreType, 0L));
                }
            }
        }


        return scores;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .toModel();
    }

    @PreAuthorize("true")
    @GetMapping("all")
    public List<DiscordServerModel> getAllServers(Authentication authentication) {
        List<String> permissions = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        Set<DiscordServerModel> servers = discordServerService.findAll();

        return servers.stream()
                .filter(server -> permissions.contains("ROLE_admin") || permissions.contains("ROLE_bot") || permissions.contains("server_" + server.getId()))
                .toList();
    }

    @GetMapping(value = "{server}/total-leaderboard")
    public LeaderboardModel getTotalLeaderboard(@PathVariable("server") long serverId, @RequestParam(required =
            false, defaultValue = "DEFAULT", value = "score-type") ScoreType scoreType, @RequestParam(required =
            false, defaultValue = "0") int page,
                                                @RequestParam(value = "user", required = false) Optional<Long> userId) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Page<ScoreModel> scores = scoreService.getTotalLeaderboard(discordServer, scoreType, page)
                .map(ScoreSum::toScoreModel);

        LeaderboardModel leaderboardModel = new LeaderboardModel(
                scores.getPageable().getPageNumber(),
                scores.getTotalPages(),
                scores.getContent()
        );

        userId.ifPresent(id -> {
            DiscordUser user = discordUserService.loadEntityOrCreate(id);

            leaderboardModel.setPlayerPosition(scoreService.getTotalPosition(discordServer, scoreType, user));
            scoreService.countTotalScoreForCarrier(user, discordServer, scoreType).ifPresent(score ->
                    leaderboardModel.setPlayerScore(score.toScoreModel()));
        });

        return leaderboardModel;
    }
}