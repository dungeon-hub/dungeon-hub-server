package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.taubsie.dungeonhub.common.enums.ScoreResetType;
import me.taubsie.dungeonhub.common.enums.ScoreType;
import me.taubsie.dungeonhub.common.model.ScoreResetModel;
import me.taubsie.dungeonhub.common.model.score.LeaderboardModel;
import me.taubsie.dungeonhub.common.model.score.ScoreModel;
import me.taubsie.dungeonhub.common.model.score.ScoreUpdateModel;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.service.CarryTypeService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.DiscordUserService;
import me.taubsie.dungeonhub.server.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/server/{server}/carry-type/{carry-type}/score")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@Tag(name = "Score")
public class ScoreController {
    private final DiscordServerService discordServerService;
    private final CarryTypeService carryTypeService;
    private final ScoreService scoreService;
    private final DiscordUserService discordUserService;

    @Autowired
    public ScoreController(DiscordServerService discordServerService, CarryTypeService carryTypeService, ScoreService scoreService, DiscordUserService discordUserService) {
        this.discordServerService = discordServerService;
        this.carryTypeService = carryTypeService;
        this.scoreService = scoreService;
        this.discordUserService = discordUserService;
    }

    @GetMapping("{id}")
    public ScoreModel getScore(@PathVariable("server") long serverId, @PathVariable("carry-type") long carryTypeId,
                               @PathVariable long id, @RequestParam(required = false, defaultValue = "DEFAULT",
            value = "score-type") ScoreType scoreType) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        DiscordUser carrier = discordUserService.loadEntityOrCreate(id);

        return scoreService.countScoreForCarrier(carrier, carryType, scoreType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .toModel();
    }

    @GetMapping("all")
    public List<ScoreModel> getScores(@PathVariable("server") long serverId,
                                      @PathVariable("carry-type") long carryTypeId,
                                      @RequestParam(required = false) Optional<Long> id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (id.isEmpty()) {
            return scoreService.getAllScores(carryType).stream()
                    .map(Score::toModel)
                    .toList();
        }

        DiscordUser carrier = discordUserService.loadEntityOrCreate(id.get());

        return scoreService.getAllScores(carrier, carryType).stream()
                .map(Score::toModel)
                .toList();
    }

    @PutMapping
    public List<ScoreModel> updateScore(@PathVariable("server") long serverId,
                                        @PathVariable("carry-type") long carryTypeId,
                                        @RequestBody ScoreUpdateModel scoreUpdateModel) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        DiscordUser carrier = discordUserService.loadEntityOrCreate(scoreUpdateModel.getId());

        return scoreService.updateAllScores(carrier, carryType, scoreUpdateModel.getAmount())
                .stream().map(Score::toModel)
                .toList();
    }

    @GetMapping(value = "total-leaderboard")
    public LeaderboardModel getTotalLeaderboard(@PathVariable("server") long serverId,
                                                @PathVariable("carry-type") long carryTypeId, @RequestParam(required =
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

    @GetMapping(value = "leaderboard")
    public LeaderboardModel getLeaderboard(@PathVariable("server") long serverId,
                                           @PathVariable("carry-type") long carryTypeId, @RequestParam(required =
            false, defaultValue = "DEFAULT", value = "score-type") ScoreType scoreType, @RequestParam(required =
            false, defaultValue = "0") int page,
                                           @RequestParam(value = "user", required = false) Optional<Long> userId) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<ScoreModel> scores = scoreService.getLeaderboard(carryType, scoreType, page).map(Score::toModel);

        LeaderboardModel leaderboardModel = new LeaderboardModel(
                scores.getPageable().getPageNumber(),
                scores.getTotalPages(),
                scores.getContent()
        );

        userId.ifPresent(id -> {
            DiscordUser user = discordUserService.loadEntityOrCreate(id);

            leaderboardModel.setPlayerPosition(scoreService.getPosition(carryType, scoreType, user));
            scoreService.countScoreForCarrier(user, carryType, scoreType).ifPresent(score ->
                    leaderboardModel.setPlayerScore(score.toModel()));
        });

        return leaderboardModel;
    }

    @DeleteMapping
    public ScoreResetModel resetScore(@PathVariable("server") long serverId, @PathVariable("carry-type") long carryTypeId, @RequestParam("score-type") ScoreResetType scoreResetType) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return scoreService.resetScores(carryType, scoreResetType);
    }
}