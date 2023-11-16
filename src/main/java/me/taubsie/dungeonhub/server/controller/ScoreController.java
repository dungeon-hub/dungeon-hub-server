package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.taubsie.dungeonhub.common.enums.ScoreType;
import me.taubsie.dungeonhub.common.model.score.LeaderboardModel;
import me.taubsie.dungeonhub.common.model.score.ScoreModel;
import me.taubsie.dungeonhub.common.model.score.ScoreUpdateModel;
import me.taubsie.dungeonhub.server.entities.CarryType;
import me.taubsie.dungeonhub.server.entities.Score;
import me.taubsie.dungeonhub.server.entities.Server;
import me.taubsie.dungeonhub.server.service.CarryTypeService;
import me.taubsie.dungeonhub.server.service.ScoreService;
import me.taubsie.dungeonhub.server.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/server/{server}/carry-type/{carry-type}/score")
@PreAuthorize("hasAuthority('server_' + @requestHelper.getPathVariable('server')) || hasAnyRole('bot', 'admin')")
@Tag(name = "Score")
public class ScoreController {
    private final ServerService serverService;
    private final CarryTypeService carryTypeService;
    private final ScoreService scoreService;

    @Autowired
    public ScoreController(ServerService serverService, CarryTypeService carryTypeService, ScoreService scoreService) {
        this.serverService = serverService;
        this.carryTypeService = carryTypeService;
        this.scoreService = scoreService;
    }

    @GetMapping("{id}")
    public ScoreModel getScore(@PathVariable("server") long serverId, @PathVariable("carry-type") long carryTypeId,
                               @PathVariable long id, @RequestParam(required = false, defaultValue = "DEFAULT",
            value = "score-type") ScoreType scoreType) {
        Server server = serverService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(server, carryTypeId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        return scoreService.countScoreForCarrier(id, carryType, scoreType)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .toModel();
    }

    @GetMapping("all")
    public List<ScoreModel> getScores(@PathVariable("server") long serverId,
                                      @PathVariable("carry-type") long carryTypeId,
                                      @RequestParam long id) {
        Server server = serverService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(server, carryTypeId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        return scoreService.getAllScores(id, carryType)
                .stream().map(Score::toModel)
                .toList();
    }

    @PutMapping
    public List<ScoreModel> updateScore(@PathVariable("server") long serverId,
                                        @PathVariable("carry-type") long carryTypeId,
                                        @RequestBody ScoreUpdateModel scoreUpdateModel) {
        Server server = serverService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(server, carryTypeId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        return scoreService.updateAllScores(scoreUpdateModel.getId(), carryType, scoreUpdateModel.getAmount())
                .stream().map(Score::toModel)
                .toList();
    }

    @GetMapping("leaderboard")
    public LeaderboardModel getLeaderboard(@PathVariable("server") long serverId,
                                           @PathVariable("carry-type") long carryTypeId, @RequestParam(required =
            false, defaultValue = "DEFAULT", value = "score-type") ScoreType scoreType, @RequestParam(required =
            false, defaultValue = "0") int page,
                                           @RequestParam(value = "user", required = false) Optional<Long> userId) {
        if (page < 0) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        Server server = serverService.getOrCreate(serverId);

        CarryType carryType = carryTypeService.loadEntityById(server, carryTypeId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Page<ScoreModel> scores = scoreService.getLeaderboard(carryType, scoreType, page).map(Score::toModel);

        LeaderboardModel leaderboardModel = new LeaderboardModel(
                scores.getPageable().getPageNumber(),
                scores.getTotalPages(),
                scores.getContent()
        );

        userId.ifPresent(user -> {
            leaderboardModel.setPlayerPosition(scoreService.getPosition(carryType, scoreType, user));
            scoreService.countScoreForCarrier(user, carryType, scoreType).ifPresent(score ->
                    leaderboardModel.setPlayerScore(score.toModel()));
        });

        return leaderboardModel;
    }
}