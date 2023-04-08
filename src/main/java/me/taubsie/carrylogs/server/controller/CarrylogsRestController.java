package me.taubsie.carrylogs.server.controller;

import me.taubsie.dungeonhub.common.CarryInformation;
import me.taubsie.dungeonhub.common.CarryLogService;
import me.taubsie.dungeonhub.common.CarryRole;
import me.taubsie.carrylogs.server.service.DatabaseService;
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
public class CarrylogsRestController {
    @GetMapping("/v1/hello")
    public ResponseEntity<String> hello(Principal principal) {
        return new ResponseEntity<>(String.format("Hello, %s!", principal.getName()), HttpStatus.OK);
    }

    @PostAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @GetMapping("/v1/admin")
    public ResponseEntity<String> admin(Principal principal) {
        return new ResponseEntity<>(String.format("Hello, %s! Welcome to the secret endpoint.", principal.getName()), HttpStatus.OK);
    }

    @PostMapping("/v1/log-queue")
    public ResponseEntity<String> addLogQueue(Long id, String carryInformation) {
        CarryInformation carry = CarryInformation.fromJson(carryInformation);
        try {
            DatabaseService.getInstance().addUserIfNotExists(carry.getCarrier());

            DatabaseService.getInstance().addToLogQueue(id, carry);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/v1/approving-queue")
    public ResponseEntity<String> addApprovingQueue(Long id, String carryInformation) {
        CarryInformation carry = CarryInformation.fromJson(carryInformation);
        try {
            DatabaseService.getInstance().addUserIfNotExists(carry.getCarrier());

            DatabaseService.getInstance().addToApprovingQueue(id, carry);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/v1/log-queue")
    public ResponseEntity<String> removeLogQueue(Long id) {
        try {
            DatabaseService.getInstance().removeFromLogQueue(id);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = {"/v1/approving-queue", "/v1/approving-queue/{id}"})
    public ResponseEntity<String> getApprovingQueue(@PathVariable(required = false) String id) {
        if(id == null) {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getApprovingQueue()), HttpStatus.OK);
        }

        try {
            Long idLong = Long.parseLong(id);
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getFromApprovingQueue(idLong), CarryLogService.getInstance().getCarryInformationSetType()), HttpStatus.OK);
        } catch(NumberFormatException numberFormatException) {
            return new ResponseEntity<>("Id is not a number.", HttpStatus.BAD_REQUEST);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"/v1/log-queue", "/v1/log-queue/{id}"})
    public ResponseEntity<String> getLogQueue(@PathVariable(required = false) Optional<String> id) {
        if(id.isEmpty()) {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getLogQueue()), HttpStatus.OK);
        }

        try {
            Long idLong = Long.parseLong(id.get());
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getFromLogQueue(idLong), CarryLogService.getInstance().getCarryInformationSetType()), HttpStatus.OK);
        } catch(NumberFormatException numberFormatException) {
            return new ResponseEntity<>("Id is not a number.", HttpStatus.BAD_REQUEST);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/v1/approving-queue")
    public ResponseEntity<String> removeApprovingQueue(Long id) {
        try {
            DatabaseService.getInstance().removeFromApprovingQueue(id);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/v1/log")
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
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"/v1/carry-score/{id}", "/v1/carry-score/{id}/{type}"})
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
                    default -> {
                        return new ResponseEntity<>("0", HttpStatus.OK);
                    }
                }
            }

            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().countScoreForCarrier(id)), HttpStatus.OK);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/v1/carry-score/{id}/{type}")
    public ResponseEntity<String> updateScore(@PathVariable Long id, @PathVariable String type, Long amount) {
        try {
            DatabaseService.getInstance().addUserIfNotExists(id);

            return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateScore(id, amount, type)),
                    HttpStatus.OK);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/v1/leaderboard/{type}")
    public ResponseEntity<String> getLeaderboard(@PathVariable String type) {
        try {
            switch(type.toLowerCase()) {
                case "dungeon", "dungeons" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getDungeonLeaderboard()), HttpStatus.OK);
                }
                case "alltime-dungeon", "alltime-dungeons" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getAlltimeDungeonLeaderboard()), HttpStatus.OK);
                }
                case "slayer" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getSlayerLeaderboard()), HttpStatus.OK);
                }
                case "alltime-slayer" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getAlltimeSlayerLeaderboard()), HttpStatus.OK);
                }
                case "kuudra" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getKuudraLeaderboard()), HttpStatus.OK);
                }
                case "alltime-kuudra" -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getAlltimeKuudraLeaderboard()), HttpStatus.OK);
                }
                default -> {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(new HashMap<>()),
                            HttpStatus.OK);
                }
            }
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/v1/role")
    public ResponseEntity<String> addRoles(Long id, String roles) {
        try {
            List<CarryRole> roleList = CarryLogService.getInstance().getGson()
                    .fromJson(roles, CarryLogService.getInstance().getCarryRoleListType());
            DatabaseService.getInstance().addRoles(id, roleList);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/v1/roles")
    public ResponseEntity<String> addMultipleRoles(String roles) {
        try {
            Map<Long, List<CarryRole>> roleData = CarryLogService.getInstance().getGson()
                    .fromJson(roles, CarryLogService.getInstance().getLongCarryRoleListMap());
            DatabaseService.getInstance().addRoles(roleData);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/v1/purge/{type}/{amount}")
    public ResponseEntity<String> getScorelessUsers(@PathVariable String type, @PathVariable long amount) {
        try {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                    DatabaseService.getInstance().getUsersWithLessScore(type, amount)), HttpStatus.OK);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}