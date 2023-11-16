package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.server.entities.CarryQueue;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueCreationModel;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueModel;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.carry_queue.CarryQueueUpdateModel;
import me.taubsie.dungeonhub.server.model.CarryQueueInitializeModel;
import me.taubsie.dungeonhub.server.repositories.CarryQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class CarryQueueService implements EntityService<CarryQueue, CarryQueueModel, CarryQueueCreationModel, CarryQueueInitializeModel, CarryQueueUpdateModel> {
    private final CarryQueueRepository carryQueueRepository;

    @Autowired
    public CarryQueueService(CarryQueueRepository carryQueueRepository) {
        this.carryQueueRepository = carryQueueRepository;
    }

    public CarryQueue getCarryQueue(Long id) {
        return carryQueueRepository.findById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }

    public void deleteCarryQueue(Long id) {
        carryQueueRepository.deleteById(id);
    }

    @Override
    public Optional<CarryQueue> loadEntityById(long id) {
        return carryQueueRepository.findById(id);
    }

    @Override
    public Optional<CarryQueue> loadEntityByName(String name) {
        return Optional.empty();
    }

    @Override
    public List<CarryQueue> findAllEntities() {
        return carryQueueRepository.findAll();
    }

    @Override
    public CarryQueue createEntity(CarryQueueInitializeModel creationModel) {
        return saveEntity(creationModel.toEntity());
    }

    @Override
    public boolean delete(long id) {
        return carryQueueRepository.findById(id).map(carryQueue ->
        {
            carryQueueRepository.delete(carryQueue);
            return true;
        }).orElse(false);
    }

    @Override
    public CarryQueue saveEntity(CarryQueue entity) {
        return carryQueueRepository.save(entity);
    }

    @Override
    public Function<CarryQueueModel, CarryQueue> toEntity() {
        return carryQueueModel -> loadEntityById(carryQueueModel.getId()).orElseThrow(() -> new EntityUnknownException(carryQueueModel.getId()));
    }

    @Override
    public Function<CarryQueue, CarryQueueModel> toModel() {
        return CarryQueue::toModel;
    }
}