package me.taubsie.carrylogs.server.controller;

import me.taubsie.carrylogs.CarryInformation;
import me.taubsie.carrylogs.CarryLogService;
import me.taubsie.carrylogs.CarryRole;
import me.taubsie.carrylogs.server.service.DatabaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
public class CarrylogsRestController
{
    @GetMapping("/v1/hello")
    public ResponseEntity<String> hello(Principal principal)
    {
        return new ResponseEntity<>(String.format("Hello, %s!", principal.getName()), HttpStatus.OK);
    }

    @PostMapping("/v1/log-queue")
    public ResponseEntity<String> addLogQueue(Long id, String carryInformation)
    {
        CarryInformation carry = CarryInformation.fromJson(carryInformation);
        try
        {
            DatabaseService.getInstance().addToLogQueue(id, carry);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/v1/approving-queue")
    public ResponseEntity<String> addApprovingQueue(Long id, String carryInformation)
    {
        CarryInformation carry = CarryInformation.fromJson(carryInformation);
        try
        {
            DatabaseService.getInstance().addToApprovingQueue(id, carry);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/v1/log-queue")
    public ResponseEntity<String> removeLogQueue(Long id)
    {
        try
        {
            DatabaseService.getInstance().removeFromLogQueue(id);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = {"/v1/approving-queue", "/v1/approving-queue/{id}"})
    public ResponseEntity<String> getApprovingQueue(@PathVariable(required = false) String id)
    {
        if (id == null)
        {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getApprovingQueue()), HttpStatus.OK);
        }

        try
        {
            Long idLong = Long.parseLong(id);
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getFromApprovingQueue(idLong), CarryLogService.getInstance().getCarryInformationSetType()), HttpStatus.OK);
        }
        catch (NumberFormatException numberFormatException)
        {
            return new ResponseEntity<>("Id is not a number.", HttpStatus.BAD_REQUEST);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"/v1/log-queue", "/v1/log-queue/{id}"})
    public ResponseEntity<String> getLogQueue(@PathVariable(required = false) Optional<String> id)
    {
        if (id.isEmpty())
        {
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getLogQueue()), HttpStatus.OK);
        }

        try
        {
            Long idLong = Long.parseLong(id.get());
            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getFromLogQueue(idLong), CarryLogService.getInstance().getCarryInformationSetType()), HttpStatus.OK);
        }
        catch (NumberFormatException numberFormatException)
        {
            return new ResponseEntity<>("Id is not a number.", HttpStatus.BAD_REQUEST);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/v1/approving-queue")
    public ResponseEntity<String> removeApprovingQueue(Long id)
    {
        try
        {
            DatabaseService.getInstance().removeFromApprovingQueue(id);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/v1/log")
    public ResponseEntity<String> logCarry(String carryInformation)
    {
        CarryInformation carry = CarryInformation.fromJson(carryInformation);

        try
        {
            DatabaseService.getInstance().logCarryInformation(carry);

            if (carry.isDungeonCarry())
            {
                return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateDungeonScore(carry.getCarrier(), carry.calculateScore())), HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateSlayerScore(carry.getCarrier(), carry.calculateScore())), HttpStatus.OK);
            }
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = {"/v1/carry-score/{id}", "/v1/carry-score/{id}/{type}"})
    public ResponseEntity<String> countScore(@PathVariable Long id, @PathVariable(required = false) Optional<String> type)
    {
        try
        {
            if (type.isPresent())
            {
                switch (type.get().toLowerCase())
                {
                    case "dungeon" ->
                    {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countDungeonScoreForCarrier(id)), HttpStatus.OK);
                    }
                    case "slayer" ->
                    {
                        return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().countSlayerScoreForCarrier(id)), HttpStatus.OK);
                    }
                }
            }

            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().countScoreForCarrier(id)), HttpStatus.OK);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/v1/carry-score/{id}/{type}")
    public ResponseEntity<String> updateScore(@PathVariable Long id, @PathVariable String type, Long amount)
    {
        try
        {
            return new ResponseEntity<>(String.valueOf(DatabaseService.getInstance().updateScore(id, amount, type)), HttpStatus.OK);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/v1/leaderboard/{type}")
    public ResponseEntity<String> getLeaderboard(@PathVariable String type)
    {
        try
        {
            switch (type.toLowerCase())
            {
                case "dungeon", "dungeons" ->
                {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getDungeonLeaderboard()), HttpStatus.OK);
                }
                case "slayer" ->
                {
                    return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(DatabaseService.getInstance().getSlayerLeaderboard()), HttpStatus.OK);
                }
            }

            return new ResponseEntity<>(CarryLogService.getInstance().getGson().toJson(new HashMap<>()), HttpStatus.OK);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/v1/role")
    public ResponseEntity<String> addRoles(Long id, List<CarryRole> roles)
    {
        try
        {
            DatabaseService.getInstance().addRoles(id, roles);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (SQLException sqlException)
        {
            sqlException.printStackTrace();
            return new ResponseEntity<>(sqlException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}