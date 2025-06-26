package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.server.entities.Reputation;
import me.taubsie.dungeonhub.server.service.ReputationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reputation")
public class ReputationController {

    private final ReputationService reputationService;

    public ReputationController(ReputationService reputationService) {
        this.reputationService = reputationService;
    }

    @GetMapping("/{userId}")
    public Reputation findUserById(@PathVariable Long userId) {
        return reputationService.findByUserId(userId);
    }
}


