package me.taubsie.carrylogs.server.controller;

import me.taubsie.carrylogs.server.exceptions.ForbiddenException;
import me.taubsie.carrylogs.server.service.DatabaseService;
import me.taubsie.dungeonhub.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/")
public class CarrylogsRestController {
    private static final Logger logger = LoggerFactory.getLogger(CarrylogsRestController.class);

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
        catch (SQLException sqlException) {
            logger.error("Error when trying to add element to log queue.", sqlException);
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
        catch (SQLException sqlException) {
            logger.error("Error when trying to add element to approving queue.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("log-queue")
    public ResponseEntity<String> removeLogQueue(Long id) {
        try {
            DatabaseService.getInstance().removeFromLogQueue(id);
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to delete element from log queue.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = {"approving-queue"})
    public ResponseEntity<String> getApprovingQueue(@RequestParam(required = false) Optional<Long> id) {
        try {
            if (id.isEmpty()) {
                return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getApprovingQueue()), HttpStatus.OK);
            }

            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getFromApprovingQueue(id.get()), CarryLogService.getInstance().getCarryInformationSetType()), HttpStatus.OK);
        }
        catch (NumberFormatException numberFormatException) {
            return new ResponseEntity<>("Id is not a number.", HttpStatus.BAD_REQUEST);
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to load approving queue.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"log-queue"})
    public ResponseEntity<String> getLogQueue(@RequestParam(required = false) Optional<String> id) {
        try {
            if (id.isEmpty()) {
                return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getLogQueue()), HttpStatus.OK);
            }

            Long idLong = Long.parseLong(id.get());
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getFromLogQueue(idLong), CarryLogService.getInstance().getCarryInformationSetType()), HttpStatus.OK);
        }
        catch (NumberFormatException numberFormatException) {
            return new ResponseEntity<>("Id is not a number.", HttpStatus.BAD_REQUEST);
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to load log queue.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("approving-queue")
    public ResponseEntity<String> removeApprovingQueue(Long id) {
        try {
            DatabaseService.getInstance().removeFromApprovingQueue(id);
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to delete element from approving queue.", sqlException);
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

            return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateScore(carry.getCarrier(),
                    carry.calculateScore(), carry.getCarryType())), HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when logging carry.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"server/{server}/carry-score/{id}", "server/{server}/carry-score/{id}/{type}"})
    public ResponseEntity<String> countScore(@PathVariable Long server,
                                             @PathVariable Long id,
                                             @PathVariable(required = false) Optional<Long> type) {
        try {
            Optional<CarryType> carryType = type.flatMap(carryTypeId -> {
                try {
                    return DatabaseService.getInstance().getCarryType(carryTypeId);
                }
                catch (SQLException sqlException) {
                    logger.error("Error while loading carry type with id {}.", carryTypeId, sqlException);
                }
                return Optional.empty();
            });

            if (type.isPresent() && carryType.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            if (carryType.isPresent() && carryType.get().getServer() != server) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(
                    carryType.isPresent()
                            ? String.valueOf(DatabaseService.getInstance().countScoreForCarrier(id, carryType.get()))
                            :
                            CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().countScoreForCarrier(server, id)),
                    HttpStatus.OK
            );
        }
        catch (SQLException sqlException) {
            logger.error("Error when loading carry score of type {}.", type.orElse(-1L), sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("carry-score/{id}/{type}")
    public ResponseEntity<String> updateScore(@PathVariable Long id, @PathVariable(name = "type") Long carryTypeId,
                                              Long amount) {
        try {
            DatabaseService.getInstance().addUserIfNotExists(id);

            Optional<CarryType> carryType = DatabaseService.getInstance().getCarryType(carryTypeId);

            if (carryType.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateScore(id, amount,
                    carryType.get())),
                    HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when modifying carry score.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = {"leaderboard/{type}/pages/{leaderboard}", "leaderboard/{type}/pages"})
    public ResponseEntity<String> getLeaderboardPages(@PathVariable long type, @PathVariable(required = false) Optional<String> leaderboard) {
        try {
            Optional<CarryType> carryType = DatabaseService.getInstance().getCarryType(type);

            if(carryType.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            LeaderboardType leaderboardType = leaderboard.flatMap(LeaderboardType::fromName).orElse(LeaderboardType.DEFAULT);

            Long entries = DatabaseService.getInstance().getLeaderboardPages(carryType.get(), leaderboardType);

            return new ResponseEntity<>(String.valueOf(entries), HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when loading leaderboard pages.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = {"leaderboard/{carry-type}/{type}", "leaderboard/{carry-type}"})
    public ResponseEntity<String> getLeaderboard(@PathVariable(name = "carry-type") long carryTypeId,
                                                 @PathVariable(required = false) Optional<String> type,
                                                 @RequestParam(required = false) Integer page) {
        if (page == null) {
            page = 1;
        }

        try {
            Optional<CarryType> carryType = DatabaseService.getInstance().getCarryType(carryTypeId);

            if (carryType.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            LeaderboardType leaderboardType = type.flatMap(LeaderboardType::fromName).orElse(LeaderboardType.DEFAULT);

            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getLeaderboard(page, carryType.get(), leaderboardType)), HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when loading leaderboard.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("role")
    public ResponseEntity<String> addRoles(Long id, String roles) {
        try {
            List<OldCarryRole> roleList = CarryLogService.getInstance().getGson()
                    .fromJson(roles, CarryLogService.getInstance().getCarryRoleListType());
            DatabaseService.getInstance().addRoles(id, roleList);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when adding a role to user.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("roles")
    public ResponseEntity<String> addMultipleRoles(String roles) {
        try {
            Map<Long, List<OldCarryRole>> roleData = CarryLogService.getInstance().getGson()
                    .fromJson(roles, CarryLogService.getInstance().getLongCarryRoleListMapType());
            DatabaseService.getInstance().addRoles(roleData);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when adding multiple roles to user.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("purge/{type}/{amount}")
    public ResponseEntity<String> getScorelessUsers(@PathVariable String type, @PathVariable long amount) {
        try {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                    DatabaseService.getInstance().getUsersWithLessScore(type, amount)), HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error when loading purge data.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("strike/{server}/all")
    public ResponseEntity<String> getAllStrikesForUser(@PathVariable long server, @RequestParam long user) {
        try {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                    DatabaseService.getInstance().getAllStrikeData(server, user)), HttpStatus.OK);
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
                return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(
                        DatabaseService.getInstance().getStrikesInServer(server)), HttpStatus.OK);
            }

            if (user.isPresent() && id.isEmpty()) {
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
            DatabaseService.getInstance().removeStrike(server, id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (ForbiddenException forbiddenException) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to remove strike.", sqlException);
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
        catch (SQLException sqlException) {
            logger.error("Error when trying to add strike.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("server/{server}/category/{category}/carry-tier")
    public ResponseEntity<String> getCarryTierFromCategory(@PathVariable long server, @PathVariable long category) {
        try {
            Optional<CarryTier> carryTier = DatabaseService.getInstance().loadCarryTierFromCategory(category);

            carryTier = carryTier.filter(tier -> tier.getCarryType().getServer() == server);

            return carryTier.map(tier -> new ResponseEntity<>(tier.toJson(), HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        }
        catch (SQLException sqlException) {
            logger.error("Error when trying to load carry tier for category {}.", category, sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("server/{server}/leaderboard-type")
    public ResponseEntity<String> getLeaderboardTypes(@PathVariable long server) {
        try {
            List<String> result = new ArrayList<>();
            List<CarryType> carryTypes = DatabaseService.getInstance().loadCarryTypesForServer(server);

            for(CarryType carryType : carryTypes) {
                result.add(carryType.getIdentifier());
                result.add("alltime-" + carryType.getIdentifier());
                result.add("event-" + carryType.getIdentifier());
            }

            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(result), HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error while trying to load score types for server {}.", server, sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("server/{server}/carry-type")
    public ResponseEntity<String> getCarryTypesForServer(@PathVariable long server,
                                                         @RequestParam(required = false) Optional<String> identifier) {
        try {
            if (identifier.isEmpty()) {
                return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().loadCarryTypesForServer(server)), HttpStatus.OK);
            }

            Optional<CarryType> carryType = DatabaseService.getInstance().getCarryType(server, identifier.get());

            return carryType.map(type -> new ResponseEntity<>(type.toJson(), HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        }
        catch (SQLException sqlException) {
            logger.error("Error while trying to load carry types of server {}.", server, sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("carry-types")
    public ResponseEntity<String> getAllCarryTypes() {
        try {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().loadCarryTypes()), HttpStatus.OK);
        }
        catch (SQLException sqlException) {
            logger.error("Error while trying to load all carry types.", sqlException);
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}