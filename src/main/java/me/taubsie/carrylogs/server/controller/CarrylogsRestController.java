package me.taubsie.carrylogs.server.controller;

import me.taubsie.carrylogs.server.exceptions.ForbiddenException;
import me.taubsie.dungeonhub.common.CarryInformation;
import me.taubsie.dungeonhub.common.CarryLogService;
import me.taubsie.dungeonhub.common.CarryRole;
import me.taubsie.carrylogs.server.service.DatabaseService;
import me.taubsie.dungeonhub.common.StrikeData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/")
public class CarrylogsRestController {
    @GetMapping("hello")
    public ResponseEntity<String> hello(Principal principal) {
        return new ResponseEntity<>(String.format("Hello, %s!", principal.getName()), HttpStatus.OK);
    }

    @PostAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @GetMapping("admin")
    public ResponseEntity<String> admin(Principal principal) {
        return new ResponseEntity<>(String.format("Hello, %s! Welcome to the secret endpoint.", principal.getName()),
                HttpStatus.OK);
    }

    @PostMapping("log-queue")
    public ResponseEntity<String> addLogQueue(Long id, String carryInformation) {
        CarryInformation carry = CarryInformation.fromJson(carryInformation);
        try {
            DatabaseService.getInstance().addUserIfNotExists(carry.getCarrier());

            DatabaseService.getInstance().addToLogQueue(id, carry);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("approving-queue")
    public ResponseEntity<String> addApprovingQueue(Long id, String carryInformation) {
        CarryInformation carry = CarryInformation.fromJson(carryInformation);
        try {
            DatabaseService.getInstance().addUserIfNotExists(carry.getCarrier());

            DatabaseService.getInstance().addToApprovingQueue(id, carry);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("log-queue")
    public ResponseEntity<String> removeLogQueue(Long id) {
        try {
            DatabaseService.getInstance().removeFromLogQueue(id);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //TODO change url to use query parameters
    @GetMapping(value = {"approving-queue", "approving-queue/{id}"})
    public ResponseEntity<String> getApprovingQueue(@PathVariable(required = false) String id) {
        try {
            if(id == null) {
                return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getApprovingQueue()), HttpStatus.OK);
            }

            Long idLong = Long.parseLong(id);
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getFromApprovingQueue(idLong), CarryLogService.getInstance().getCarryInformationSetType()), HttpStatus.OK);
        }
        catch(NumberFormatException numberFormatException) {
            return new ResponseEntity<>("Id is not a number.", HttpStatus.BAD_REQUEST);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //TODO change url to use query parameters
    @GetMapping(value = {"log-queue", "log-queue/{id}"})
    public ResponseEntity<String> getLogQueue(@PathVariable(required = false) Optional<String> id) {
        try {
            if(id.isEmpty()) {
                return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getLogQueue()), HttpStatus.OK);
            }

            Long idLong = Long.parseLong(id.get());
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getFromLogQueue(idLong), CarryLogService.getInstance().getCarryInformationSetType()), HttpStatus.OK);
        }
        catch(NumberFormatException numberFormatException) {
            return new ResponseEntity<>("Id is not a number.", HttpStatus.BAD_REQUEST);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("approving-queue")
    public ResponseEntity<String> removeApprovingQueue(Long id) {
        try {
            DatabaseService.getInstance().removeFromApprovingQueue(id);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("log")
    public ResponseEntity<String> logCarry(String carryInformation) {
        CarryInformation carry = CarryInformation.fromJson(carryInformation);

        try {
            DatabaseService.getInstance().addUserIfNotExists(carry.getCarrier());

            DatabaseService.getInstance().logCarryInformation(carry);

            if(carry.isDungeonCarry()) {
                return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateDungeonScore(carry.getCarrier(), carry.calculateScore())), HttpStatus.OK);
            } else if(carry.isKuudraCarry()) {
                return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateKuudraScore(carry.getCarrier(), carry.calculateScore())), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateSlayerScore(carry.getCarrier(), carry.calculateScore())), HttpStatus.OK);
            }
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"carry-score/{id}", "carry-score/{id}/{type}"})
    public ResponseEntity<String> countScore(@PathVariable Long id,
                                             @PathVariable(required = false) Optional<String> type) {
        try {
            if(type.isPresent()) {
                switch(type.get().toLowerCase()) {
                    case "dungeon" -> {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countDungeonScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "kuudra" -> {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countKuudraScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "slayer" -> {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countSlayerScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "alltime-dungeon" -> {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countAlltimeDungeonScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "alltime-slayer" -> {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countAlltimeSlayerScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "alltime-kuudra" -> {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countAlltimeKuudraScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "event-dungeon" -> {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countEventDungeonScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "event-slayer" -> {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countEventSlayerScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "event-kuudra" -> {
                        //lmao this isn't even implemented yet?!
                        return new ResponseEntity<>(String.valueOf(0L), HttpStatus.OK);
                    }
                    default -> {
                        return new ResponseEntity<>("0", HttpStatus.OK);
                    }
                }
            }

            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().countScoreForCarrier(id)), HttpStatus.OK);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("carry-score/{id}/{type}")
    public ResponseEntity<String> updateScore(@PathVariable Long id, @PathVariable String type, Long amount) {
        try {
            DatabaseService.getInstance().addUserIfNotExists(id);

            return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateScore(id, amount, type)),
                    HttpStatus.OK);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("leaderboard/{type}/pages")
    public ResponseEntity<String> getLeaderboardPages(@PathVariable String type) {
        try {
            Long entries = DatabaseService.getInstance().getLeaderboardPages(type);

            return new ResponseEntity<>(String.valueOf(entries), HttpStatus.OK);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("leaderboard/{type}")
    public ResponseEntity<String> getLeaderboard(@PathVariable String type,
                                                 @RequestParam(required = false) Integer page) {
        if(page == null) {
            page = 1;
        }

        try {
            switch(type.toLowerCase()) {
                case "dungeon", "dungeons" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getDungeonLeaderboard(page)), HttpStatus.OK);
                }
                case "alltime-dungeon", "alltime-dungeons" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getAlltimeDungeonLeaderboard(page)), HttpStatus.OK);
                }
                case "slayer" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getSlayerLeaderboard(page)), HttpStatus.OK);
                }
                case "alltime-slayer" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getAlltimeSlayerLeaderboard(page)), HttpStatus.OK);
                }
                case "kuudra" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getKuudraLeaderboard(page)), HttpStatus.OK);
                }
                case "alltime-kuudra" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getAlltimeKuudraLeaderboard(page)), HttpStatus.OK);
                }
                case "event-dungeon" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getEventDungeonLeaderboard(page)), HttpStatus.OK);
                }
                case "event-slayer" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getEventSlayerLeaderboard(page)), HttpStatus.OK);
                }
                default -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(new HashMap<>()),
                            HttpStatus.OK);
                }
            }
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("role")
    public ResponseEntity<String> addRoles(Long id, String roles) {
        try {
            List<CarryRole> roleList = CarryLogService.getInstance().getGson()
                    .fromJson(roles, CarryLogService.getInstance().getCarryRoleListType());
            DatabaseService.getInstance().addRoles(id, roleList);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("roles")
    public ResponseEntity<String> addMultipleRoles(String roles) {
        try {
            Map<Long, List<CarryRole>> roleData = CarryLogService.getInstance().getGson()
                    .fromJson(roles, CarryLogService.getInstance().getLongCarryRoleListMap());
            DatabaseService.getInstance().addRoles(roleData);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("purge/{type}/{amount}")
    public ResponseEntity<String> getScorelessUsers(@PathVariable String type, @PathVariable long amount) {
        try {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                    DatabaseService.getInstance().getUsersWithLessScore(type, amount)), HttpStatus.OK);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("strike/{server}/all")
    public ResponseEntity<String> getAllStrikesForUser(@PathVariable long server, @RequestParam long user) {
        try {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                    DatabaseService.getInstance().getAllStrikeData(server, user)), HttpStatus.OK);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("strike/{server}")
    public ResponseEntity<String> getStrikeData(@PathVariable long server,
                                                @RequestParam(required = false) Optional<Long> user,
                                                @RequestParam(required = false) Optional<Long> id) {
        try {
            if(user.isEmpty() && id.isEmpty()) {
                return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                        DatabaseService.getInstance().getStrikesInServer(server)), HttpStatus.OK);
            }

            if(user.isPresent() && id.isEmpty()) {
                return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                        DatabaseService.getInstance().getValidStrikeData(server, user.get())), HttpStatus.OK);
            }

            return DatabaseService.getInstance().getStrikeDataById(id.get())
                    .filter(data -> data.getServer() == server)
                    .filter(data -> user.isEmpty() || data.getUser() == user.get())
                    .map(data -> new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(data),
                            HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("strike/{server}/{id}")
    public ResponseEntity<String> removeStrike(@PathVariable long server, @PathVariable long id) {
        try {
            DatabaseService.getInstance().removeStrike(server, id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(ForbiddenException forbiddenException) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("strike")
    public ResponseEntity<String> insertNewStrike(String strikeData) {
        StrikeData strikeDataObj = CarryLogService.getInstance().getGson().fromJson(strikeData, StrikeData.class);

        try {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                    DatabaseService.getInstance().insertStrikeData(strikeDataObj)), HttpStatus.OK);
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}