package me.taubsie.dungeonhub.server.service;

import me.taubsie.dungeonhub.server.DungeonHubServerApplication;
import me.taubsie.dungeonhub.server.entities.*;
import me.taubsie.dungeonhub.server.repositories.CarryRepository;
import net.dungeonhub.model.carry.CarryCountsModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// application.properties requires an external config file, so it is skipped here and the H2 migrations are used directly
@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/none/",
        "spring.flyway.locations=classpath:migrations/{vendor}",
        "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = DungeonHubServerApplication.class)
class CarryServiceTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarryRepository carryRepository;

    private CarryService carryService;

    private DiscordServer server;
    private DiscordUser carrier;
    private DiscordUser player;

    @BeforeEach
    void setUp() {
        carryService = new CarryService(carryRepository);

        server = entityManager.persist(new DiscordServer(1L));
        carrier = entityManager.persist(new DiscordUser(10L));
        player = entityManager.persist(new DiscordUser(11L));
    }

    @Test
    void getCarryCounts_sumsAmountPerCarryType() {
        CarryType dungeons = entityManager.persist(new CarryType("dungeons", "Dungeons", server, null, null));
        CarryType slayer = entityManager.persist(new CarryType("slayer", "Slayer", server, null, null));
        CarryType unused = entityManager.persist(new CarryType("kuudra", "Kuudra", server, null, null));

        CarryDifficulty f7 = createDifficulty(dungeons, "f7");
        CarryDifficulty m7 = createDifficulty(dungeons, "m7");
        CarryDifficulty t4 = createDifficulty(slayer, "t4");
        createDifficulty(unused, "t5");

        logCarry(carrier, f7, 5);
        logCarry(carrier, m7, 2);
        logCarry(carrier, t4, 3);
        logCarry(player, t4, 7);

        DiscordServer otherServer = entityManager.persist(new DiscordServer(2L));
        CarryType otherType = entityManager.persist(new CarryType("dungeons", "Dungeons", otherServer, null, null));
        logCarry(carrier, createDifficulty(otherType, "f7"), 11);

        entityManager.flush();

        CarryCountsModel carryCounts = carryService.getCarryCounts(server, carrier);

        assertEquals(3, carryCounts.getLogCount());
        assertEquals(10, carryCounts.getCarryCount());
        assertEquals(Map.of(dungeons.getId(), 7L, slayer.getId(), 3L), carryCounts.getByType());
        // the existing total still counts carry logs, not amounts
        assertEquals(3, carryService.countCarries(server, carrier));
    }

    @Test
    void getCarryCounts_returnsZeroWithoutCarries() {
        CarryCountsModel carryCounts = carryService.getCarryCounts(server, carrier);

        assertEquals(0, carryCounts.getLogCount());
        assertEquals(0, carryCounts.getCarryCount());
        assertTrue(carryCounts.getByType().isEmpty());
    }

    private CarryDifficulty createDifficulty(CarryType carryType, String identifier) {
        CarryTier carryTier = entityManager.persist(new CarryTier(identifier, identifier, carryType, null, null, null, null, null));
        return entityManager.persist(new CarryDifficulty(identifier, identifier, carryTier, null, null, null, null, 0, 1, null));
    }

    private void logCarry(DiscordUser carryCarrier, CarryDifficulty carryDifficulty, int amount) {
        entityManager.persist(new Carry(carryCarrier, player, amount, carryDifficulty, null, Instant.now()));
    }
}
