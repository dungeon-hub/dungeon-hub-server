package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.entities.Carry;
import me.taubsie.dungeonhub.server.entities.DiscordServer;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.repositories.CarryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarryService {
    private final CarryRepository carryRepository;

    @Autowired
    public CarryService(CarryRepository carryRepository) {
        this.carryRepository = carryRepository;
    }

    public int countCarries(DiscordServer server, DiscordUser user) {
        return carryRepository.countCarryByCarryDifficulty_CarryTier_CarryType_DiscordServerAndCarrier(server, user);
    }

    public List<Carry> getCarries(DiscordServer server) {
        return carryRepository.getCarriesByCarryDifficulty_CarryTier_CarryType_DiscordServer(server);
    }

    public Carry saveCarry(Carry carry) {
        return carryRepository.save(carry);
    }
}