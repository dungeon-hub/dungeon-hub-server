package me.taubsie.dungeonhub.server.controller;

import me.taubsie.dungeonhub.common.DungeonHubService;
import me.taubsie.dungeonhub.common.StrikeData;
import me.taubsie.dungeonhub.server.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/")
public class DungeonHubRestController {
    private static final Logger logger = LoggerFactory.getLogger(DungeonHubRestController.class);

    private final DatabaseConfig databaseService;

    @Autowired
    public DungeonHubRestController(DatabaseConfig databaseService) {
        this.databaseService = databaseService;
    }

    @GetMapping("hello")
    public ResponseEntity<String> hello(Principal principal) {
        return new ResponseEntity<>(String.format("Hello, %s!", principal.getName()), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("admin")
    public ResponseEntity<String> admin(Principal principal) {
        return new ResponseEntity<>(String.format("Hello, %s! Welcome to the secret endpoint.", principal.getName()),
                HttpStatus.OK);
    }

    //TODO replace everything below with its own controller

    @GetMapping("strike/{server}/all")
    public ResponseEntity<String> getAllStrikesForUser(@PathVariable long server, @RequestParam long user) {
        try {
            return new ResponseEntity<>(DungeonHubService.getInstance().getGson().toJson(
                    databaseService.getAllStrikeData(server, user)), HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to load all strikes.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("strike/{server}")
    public ResponseEntity<String> getStrikeData(@PathVariable long server,
                                                @RequestParam(required = false) Optional<Long> user,
                                                @RequestParam(required = false) Optional<Long> id) {
        try {
            if (user.isEmpty() && id.isEmpty()) {
                return new ResponseEntity<>(DungeonHubService.getInstance().getGson().toJson(
                        databaseService.getStrikesInServer(server)), HttpStatus.OK);
            }

            if (user.isPresent() && id.isEmpty()) {
                return new ResponseEntity<>(DungeonHubService.getInstance().getGson().toJson(
                        databaseService.getValidStrikeData(server, user.get())), HttpStatus.OK);
            }

            return databaseService.getStrikeDataById(id.get())
                    .filter(data -> data.getServer() == server)
                    .filter(data -> user.isEmpty() || data.getUser() == user.get())
                    .map(data -> new ResponseEntity<>(DungeonHubService.getInstance().getGson().toJson(data),
                            HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to load strikes.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("strike/{server}/{id}")
    public ResponseEntity<String> removeStrike(@PathVariable long server, @PathVariable long id) {
        try {
            //TODO does it make sense to just return the strike data removed?
            //TODO return something if strike already exists
            databaseService.removeStrike(server, id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to remove strike.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("strike")
    public ResponseEntity<String> insertNewStrike(String strikeData) {
        StrikeData strikeDataObj = DungeonHubService.getInstance().getGson().fromJson(strikeData, StrikeData.class);

        try {
            return new ResponseEntity<>(DungeonHubService.getInstance().getGson().toJson(
                    databaseService.insertStrikeData(strikeDataObj)), HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to add strike.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}