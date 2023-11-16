package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.repositories.CarryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarryService {
    private final CarryRepository carryRepository;

    @Autowired
    public CarryService(CarryRepository carryRepository) {
        this.carryRepository = carryRepository;
    }

    public Carry saveCarry(Carry carry) {
        return carryRepository.save(carry);
    }
}