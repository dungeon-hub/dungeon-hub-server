package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.model.CarryQueueInitializeModel;
import me.taubsie.dungeonhub.server.service.*;
import net.dungeonhub.enums.IngameCarryType;
import net.dungeonhub.enums.QueueStep;
import net.dungeonhub.enums.TicketState;
import net.dungeonhub.model.carry_queue.CarryQueueCreationModel;
import net.dungeonhub.model.carry_queue.CarryQueueModel;
import net.dungeonhub.model.carry_queue.CarryQueueUpdateModel;
import net.dungeonhub.model.carry_queue.IngameQueueCreationModel;
import net.dungeonhub.model.score.LoggedCarryModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/queue")
@PreAuthorize("hasAnyRole('bot', 'admin')")
@Tag(name = "Carry-Queue")
public class QueueController {
    private final CarryDifficultyService carryDifficultyService;
    private final CarryQueueService carryQueueService;
    private final CarryService carryService;
    private final ScoreService scoreService;
    private final DiscordUserService discordUserService;
    private final TicketService ticketService;

    @PostMapping(value = {"carry-difficulty/{carry-difficulty}"})
    @ResponseStatus(HttpStatus.CREATED)
    public CarryQueueModel addNewQueue(@PathVariable("carry-difficulty") long carryDifficultyId,
                                       @RequestBody CarryQueueCreationModel creationModel) {
        CarryDifficulty carryDifficulty = carryDifficultyService.loadEntityById(carryDifficultyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        DiscordUser player = discordUserService.loadEntityOrCreate(creationModel.getPlayer());
        DiscordUser carrier = discordUserService.loadEntityOrCreate(creationModel.getCarrier());

        return carryQueueService.createEntity(new CarryQueueInitializeModel(carryDifficulty, player, carrier, true)
                        .fromCreationModel(creationModel))
                .toModel();
    }

    @GetMapping("unnotified")
    public List<CarryQueueModel> getUnnotifiedQueues() {
        return carryQueueService.getUnnotifiesQueues().stream().map(CarryQueue::toModel).toList();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("ingame-log")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CarryQueueModel> ingameLog(@RequestBody IngameQueueCreationModel ingameQueueCreationModel, Authentication authentication) {
        if(!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> claims = jwt.getClaims();
        if(!(claims.get("discord-id") instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        DiscordUser carrier = discordUserService.loadEntityOrCreate(userId);

        IngameCarryType ingameCarryType = ingameQueueCreationModel.getType();

        List<CarryQueueModel> response = new ArrayList<>();

        for(Long ticketId : ingameQueueCreationModel.getTicketIds()) {
            Ticket ticket = ticketService.loadEntityById(ticketId)
                    .filter(t -> t.getState() != TicketState.Deleted)
                    .filter(t -> t.getClaimer().getId() == carrier.getId()) // TODO also allow additional claimers to log
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            CarryTier carryTier = ticket.getTicketPanel().getRelatedCarryTier();

            if(carryTier == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            List<CarryDifficulty> possibleCarryDifficulties = carryTier.getCarryDifficulties().stream()
                    .filter(carryDifficulty -> carryDifficulty.getIngameCarryType() != null && carryDifficulty.getIngameCarryType().includes(ingameCarryType))
                    .toList();

            if(possibleCarryDifficulties.size() != 1) {
                continue;
            }

            CarryDifficulty carryDifficulty = possibleCarryDifficulties.getFirst();

            CarryQueueCreationModel carryQueueCreationModel = new CarryQueueCreationModel(
                    QueueStep.Transcript,
                    carrier.getId(),
                    ticket.getUser().getId(),
                    1,
                    ticket.getDiscordChannel().getId(),
                    null,
                    Instant.now()
            );

            response.add(carryQueueService.createEntity(
                    new CarryQueueInitializeModel(carryDifficulty, ticket.getUser(), carrier, false)
                            .fromCreationModel(carryQueueCreationModel)
            ).toModel());
        }

        return response;
    }

    //TODO custom methods in service / repository?
    @GetMapping("all")
    public Set<CarryQueueModel> getCarryQueues(@RequestParam(required = false, value = "related-id") Optional<Long> relatedId, @RequestParam(required = false, value = "queue-step") Optional<QueueStep> queueStep) {
        return carryQueueService.findAllEntities()
                .stream()
                .filter(carryQueue -> relatedId.isEmpty() || carryQueue.getRelationId().equals(relatedId.get()))
                .filter(carryQueue -> queueStep.isEmpty() || carryQueue.getQueueStep().equals(queueStep.get()))
                .map(CarryQueue::toModel)
                .collect(Collectors.toSet());
    }

    @PutMapping("{id}")
    public CarryQueueModel updateQueue(@PathVariable Long id, @RequestBody CarryQueueUpdateModel updateModel) {
        return carryQueueService.update(id, updateModel).toModel();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQueue(@PathVariable Long id) {
        return new ResponseEntity<>(carryQueueService.delete(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @PostMapping("log/{id}")
    public LoggedCarryModel logCarry(@PathVariable Long id, @RequestBody CarryQueueUpdateModel updateModel) {
        try {
            CarryQueue carryQueue = carryQueueService.getCarryQueue(id);
            carryQueue = carryQueueService.update(carryQueue, updateModel);
            Carry carry = carryQueue.toCarry();

            carry.setApprover(updateModel.getApprover());

            carryQueueService.deleteCarryQueue(carryQueue.getId());

            return new LoggedCarryModel(
                    carryService.saveCarry(carry).toModel(),
                    scoreService.updateAllScores(carry.getCarrier(), carry.getCarryType(), carry.calculateScore())
                            .stream().map(Score::toModel).toList()
            );
        }
        catch (NumberFormatException | UnsupportedOperationException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}