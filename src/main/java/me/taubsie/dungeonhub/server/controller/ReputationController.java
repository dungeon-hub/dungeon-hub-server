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
    public Reputation findUserById(@PathVariable int userId) {
        return reputationService.findByUserId(userId);
    }

    @PostMapping
    public Reputation save(@RequestBody Reputation reputation) {
        return reputationService.save(reputation);
    }
}

