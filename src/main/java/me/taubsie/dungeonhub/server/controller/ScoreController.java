package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.service.CarryTypeService;
import me.taubsie.dungeonhub.server.service.DiscordServerService;
import me.taubsie.dungeonhub.server.service.DiscordUserService;
import me.taubsie.dungeonhub.server.service.ScoreService;
import net.dungeonhub.enums.ScoreResetType;
import net.dungeonhub.enums.ScoreType;
import net.dungeonhub.model.score.LeaderboardModel;
import net.dungeonhub.model.score.ScoreModel;
import net.dungeonhub.model.score.ScoreResetModel;
import net.dungeonhub.model.score.ScoreUpdateModel;
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

        Optional<DiscordUser> user = userId.map(discordUserService::loadEntityOrCreate);

        return new LeaderboardModel(
                scores.getPageable().getPageNumber(),
                scores.getTotalPages(),
                scores.getContent(),
                user.map(userEntity -> scoreService.getTotalPosition(discordServer, scoreType, userEntity)).orElse(null),
                user.flatMap(userEntity -> scoreService.countTotalScoreForCarrier(userEntity, discordServer, scoreType).map(ScoreSum::toScoreModel)).orElse(null)
        );
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

        Optional<DiscordUser> user = userId.map(discordUserService::loadEntityOrCreate);

        return new LeaderboardModel(
                scores.getPageable().getPageNumber(),
                scores.getTotalPages(),
                scores.getContent(),
                user.map(userEntity -> scoreService.getTotalPosition(discordServer, scoreType, userEntity)).orElse(null),
                user.flatMap(userEntity -> scoreService.countTotalScoreForCarrier(userEntity, discordServer, scoreType).map(ScoreSum::toScoreModel)).orElse(null)
        );
    }

    @DeleteMapping
    public ScoreResetModel resetScore(@PathVariable("server") long serverId, @PathVariable("carry-type") long carryTypeId, @RequestParam("score-type") ScoreResetType scoreResetType) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(discordServer, carryTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return scoreService.resetScores(carryType, scoreResetType);
    }
}