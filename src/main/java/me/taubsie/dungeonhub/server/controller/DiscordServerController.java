package me.taubsie.dungeonhub.server.controller;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.service.*;
import net.dungeonhub.enums.ScoreType;
import net.dungeonhub.model.carry_difficulty.CarryDifficultyModel;
import net.dungeonhub.model.carry_tier.CarryTierModel;
import net.dungeonhub.model.discord_server.DiscordServerModel;
import net.dungeonhub.model.reputation.ReputationLeaderboardModel;
import net.dungeonhub.model.reputation.ReputationModel;
import net.dungeonhub.model.reputation.ReputationSumModel;
import net.dungeonhub.model.score.ScoreLeaderboardModel;
import net.dungeonhub.model.score.ScoreModel;
import net.dungeonhub.model.static_message.StaticMessageModel;
import net.dungeonhub.model.ticket.TicketModel;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
    private final CarryService carryService;
    private final ReputationService reputationService;
    private final StaticMessageService staticMessageService;
    private final TicketService ticketService;

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
            for (ScoreType scoreType : ScoreType.getEntries()) {
                if (scores.stream()
                        .filter(scoreModel -> scoreModel.getScoreType() == scoreType)
                        .filter(scoreModel -> scoreModel.getCarryType() != null)
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

        return carryTierService.findAllEntities(discordServer)
                .map(CarryTier::toModel)
                .toList();
    }

    @GetMapping("{server}/carry-difficulties")
    public List<CarryDifficultyModel> getAllCarryDifficulties(@PathVariable("server") long serverId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return carryDifficultyService.findAllEntities(discordServer)
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

    @GetMapping("{server}/reputation/{id}")
    public ReputationModel getReputation(@PathVariable("server") long serverId, @PathVariable long id) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return reputationService.loadEntityById(discordServer, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .toModel();
    }

    @PreAuthorize("true")
    @GetMapping("all")
    public List<DiscordServerModel> getAllServers(Authentication authentication) {
        List<String> permissions = Optional.ofNullable(authentication)
                .map(Authentication::getAuthorities)
                .orElse(List.of()).stream()
                .map(GrantedAuthority::getAuthority).toList();

        Set<DiscordServerModel> servers = discordServerService.findAll();

        return servers.stream()
                .filter(server -> permissions.contains("ROLE_admin") || permissions.contains("ROLE_bot") || permissions.contains("server_" + server.getId()))
                .toList();
    }

    @GetMapping(value = "{server}/total-leaderboard")
    public ScoreLeaderboardModel getTotalLeaderboard(@PathVariable("server") long serverId, @RequestParam(required =
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

        return new ScoreLeaderboardModel(
                scores.getPageable().getPageNumber(),
                scores.getTotalPages(),
                scores.getContent(),
                user.map(userEntity -> scoreService.getTotalPosition(discordServer, scoreType, userEntity)).orElse(null),
                user.flatMap(userEntity -> scoreService.countTotalScoreForCarrier(userEntity, discordServer, scoreType).map(ScoreSum::toScoreModel)).orElse(null)
        );
    }

    @GetMapping(value = "{server}/reputation-leaderboard")
    public ReputationLeaderboardModel getReputationLeaderboard(
            @PathVariable("server") long serverId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(value = "user", required = false) Optional<Long> userId
    ) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Page<ReputationSumModel> reputation = reputationService.getReputationLeaderboard(discordServer, page)
                .map(ReputationSum::toReputationSumModel);

        Optional<DiscordUser> user = userId.map(discordUserService::loadEntityOrCreate);

        return new ReputationLeaderboardModel(
                reputation.getPageable().getPageNumber(),
                reputation.getTotalPages(),
                reputation.getContent(),
                user.map(userEntity -> reputationService.getPosition(discordServer, userEntity)).orElse(null),
                user.map(userEntity -> reputationService.calculateReputation(discordServer, userEntity))
                        .map(count -> new ReputationSum(user.get(), count))
                        .map(ReputationSum::toReputationSumModel)
                        .orElse(null)
        );
    }

    @GetMapping("{server}/total-money-spent")
    public long getTotalAmountOfMoneySpentOnServices(@PathVariable("server") long serverId, @RequestParam(required = false, value = "user") Long userId, @RequestParam(required = false, value = "carrier") Long carrierId, @RequestParam(required = false, value = "carry-type") Long carryTypeId, @RequestParam(required = false, value = "carry-tier") Long carryTierId, @RequestParam(required = false, value = "since") Optional<Instant> since) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        List<Carry> carries = carryService.getCarries(discordServer);

        return carries.stream()
                .filter(carry -> userId == null || carry.getPlayer().getId() == userId)
                .filter(carry -> carrierId == null || carry.getCarrier().getId() == carrierId)
                .filter(carry -> carryTypeId == null || carry.getCarryType().getId() == carryTypeId)
                .filter(carry -> carryTierId == null || carry.getCarryTier().getId() == carryTierId)
                .filter(carry -> since.isEmpty() || since.get().isBefore(carry.getTime()))
                .mapToLong(Carry::calculatePrice)
                .sum();
    }

    @GetMapping("{server}/count-carries")
    public long countCarries(@PathVariable("server") long serverId, @RequestParam(required = false, value = "since") Optional<Instant> since) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        return since
                .map(instant -> carryService.getCarriesSince(discordServer, instant))
                .orElseGet(() -> carryService.getCarries(discordServer))
                .stream().mapToLong(Carry::getAmount)
                .sum();
    }

    @PreAuthorize("hasAnyRole('bot', 'admin')")
    @GetMapping("static-messages")
    public List<StaticMessageModel> getStaticMessages() {
        return staticMessageService.findAllEntities().stream()
                .map(StaticMessage::toModel)
                .toList();
    }

    @GetMapping("{server}/ticket/find")
    public List<TicketModel> findTickets(@PathVariable("server") long serverId, @RequestParam(name = "channel", required = false) Optional<Long> channelId) {
        DiscordServer discordServer = discordServerService.getOrCreate(serverId);

        Stream<Ticket> result = ticketService.loadEntitiesByServer(discordServer).stream();

        if(channelId.isPresent()) {
            result = result.filter(ticket -> ticket.getDiscordChannel() != null && ticket.getDiscordChannel().getId() == channelId.get());
        }

        return result.map(Ticket::toModel).toList();
    }
}