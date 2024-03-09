package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.taubsie.dungeonhub.common.enums.QueueStep;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueCreationModel;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueModel;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueUpdateModel;
import me.taubsie.dungeonhub.common.model.score.LoggedCarryModel;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.model.CarryQueueInitializeModel;
import me.taubsie.dungeonhub.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/queue")
@PreAuthorize("hasAnyRole('bot', 'admin')")
@Tag(name = "Carry-Queue")
public class QueueController {
    private final CarryDifficultyService carryDifficultyService;
    private final CarryQueueService carryQueueService;
    private final CarryService carryService;
    private final ScoreService scoreService;
    private final DiscordUserService discordUserService;

    @Autowired
    public QueueController(CarryDifficultyService carryDifficultyService, CarryQueueService carryQueueService,
                           CarryService carryService, ScoreService scoreService,
                           DiscordUserService discordUserService) {
        this.carryDifficultyService = carryDifficultyService;
        this.carryQueueService = carryQueueService;
        this.carryService = carryService;
        this.scoreService = scoreService;
        this.discordUserService = discordUserService;
    }

    @PostMapping(value = {"carry-difficulty/{carry-difficulty}"})
    @ResponseStatus(HttpStatus.CREATED)
    public CarryQueueModel addNewQueue(@PathVariable("carry-difficulty") long carryDifficultyId,
                                       @RequestBody CarryQueueCreationModel creationModel) {
        CarryDifficulty carryDifficulty = carryDifficultyService.loadEntityById(carryDifficultyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        DiscordUser player = discordUserService.loadEntityOrCreate(creationModel.getPlayer());
        DiscordUser carrier = discordUserService.loadEntityOrCreate(creationModel.getCarrier());

        return carryQueueService.createEntity(new CarryQueueInitializeModel(carryDifficulty, player, carrier)
                        .fromCreationModel(creationModel))
                .toModel();
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
            CarryQueue carryQueue = carryQueueService.getCarryQueue(id)
                    .fromModel(updateModel.apply(carryQueueService.getCarryQueue(id).toModel()));

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