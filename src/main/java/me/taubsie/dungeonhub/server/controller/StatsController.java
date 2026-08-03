package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.service.*;
import net.dungeonhub.model.stats.DiscordServerStatsModel;
import net.dungeonhub.model.stats.GlobalStatsModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/stats")
@PreAuthorize("isAuthenticated()")
public class StatsController {
    private final DiscordUserService discordUserService;
    private final CarryService carryService;
    private final TicketService ticketService;
    private final DiscordServerService discordServerService;
    private final ScoreService scoreService;
    private final AuthenticationService authenticationService;

    @SecurityRequirements()
    @PreAuthorize("permitAll()")
    @GetMapping("global")
    public GlobalStatsModel getGlobalStats() {
        return new GlobalStatsModel(
                discordUserService.countLinkedUsers(),
                carryService.getGlobalCarryStats(),
                ticketService.getGlobalTicketStats(),
                carryService.getGlobalCarrierStats()
        );
    }

    @PreAuthorize("permitAll()")
    @GetMapping("server/{server}/stats")
    public DiscordServerStatsModel getServerStats(@PathVariable("server") long serverId, Authentication authentication) {
        DiscordServer discordServer = discordServerService.loadEntityById(serverId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Optional<Long> discordUserId = Optional.ofNullable(authentication).flatMap(authenticationService::getLoggedInDiscordId);

        List<Carry> carries = carryService.getCarries(discordServer);

        return new DiscordServerStatsModel(
                carries.stream().mapToLong(Carry::calculateTotalPrice).sum(),
                carries.stream().mapToLong(Carry::getAmount).sum(),
                ticketService.countAllTickets(discordServer),
                carryService.getCarriersByDiscordServer(discordServer),
                scoreService.getTotalScore(discordServer),
                discordUserId.map(user ->
                        carries.stream().filter(carry -> carry.getPlayer().getId() == user).mapToLong(Carry::calculateTotalPrice).sum()
                ).orElse(null),
                discordUserId.map(user ->
                        carries.stream().filter(carry -> carry.getCarrier().getId() == user).mapToLong(Carry::calculateTotalPrice).sum()
                ).orElse(null),
                discordUserId.map(user ->
                        carries.stream().filter(carry -> carry.getCarrier().getId() == user).mapToLong(Carry::getAmount).sum()
                ).orElse(null),
                discordUserId.map(user ->
                        carries.stream().filter(carry -> carry.getPlayer().getId() == user).mapToLong(Carry::getAmount).sum()
                ).orElse(null)
        );
    }
}