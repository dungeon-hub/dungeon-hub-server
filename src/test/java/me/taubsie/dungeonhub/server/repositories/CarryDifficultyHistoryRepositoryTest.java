package me.taubsie.dungeonhub.server.repositories;

import me.taubsie.dungeonhub.server.entities.CarryDifficulty;
import me.taubsie.dungeonhub.server.entities.CarryDifficultyHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the H2 migrations and checks the history mapping and queries against the resulting schema.
 */
@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/no-application-properties/",
        "spring.flyway.locations=classpath:migrations/h2",
        "spring.flyway.default-schema=flyway",
        "spring.jpa.hibernate.ddl-auto=none"
})
class CarryDifficultyHistoryRepositoryTest {
    private static final Instant JANUARY = Instant.parse("2025-01-01T00:00:00Z");
    private static final Instant FEBRUARY = Instant.parse("2025-02-01T00:00:00Z");
    private static final Instant MARCH = Instant.parse("2025-03-01T00:00:00Z");

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("me.taubsie.dungeonhub.server.entities")
    static class Config {
    }

    @Autowired
    private CarryDifficultyHistoryRepository historyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CarryDifficulty first;
    private CarryDifficulty second;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("insert into \"dungeon-hub\".server (id) values (1)");
        jdbcTemplate.update("insert into \"dungeon-hub\".carry_type (id, identifier, display_name, server) "
                + "values (1, 'dungeons', 'Dungeons', 1)");
        jdbcTemplate.update("insert into \"dungeon-hub\".carry_tier (id, identifier, display_name, carry_type) "
                + "values (1, 'floor-7', 'Floor 7', 1)");
        jdbcTemplate.update("insert into \"dungeon-hub\".carry_difficulty "
                + "(id, identifier, display_name, carry_tier, score, price) values (1, 'first', 'First', 1, 1, 100)");
        jdbcTemplate.update("insert into \"dungeon-hub\".carry_difficulty "
                + "(id, identifier, display_name, carry_tier, score, price) values (2, 'second', 'Second', 1, 1, 100)");

        first = entityManager.find(CarryDifficulty.class, 1L);
        second = entityManager.find(CarryDifficulty.class, 2L);
    }

    @Test
    void historyIsStoredAndLoadedWithAllValues() {
        historyRepository.save(new CarryDifficultyHistory(first, 100, 80, 5, 3, JANUARY, FEBRUARY));
        entityManager.flush();
        entityManager.clear();

        CarryDifficultyHistory loaded = historyRepository.findAllByCarryDifficulty_IdIn(Set.of(1L)).getFirst();

        assertEquals(1L, loaded.getCarryDifficulty().getId());
        assertEquals(100, loaded.getPrice());
        assertEquals(80, loaded.getBulkPrice());
        assertEquals(5, loaded.getBulkAmount());
        assertEquals(3, loaded.getScore());
        assertEquals(JANUARY, loaded.getDateFrom());
        assertEquals(FEBRUARY, loaded.getDateTo());
    }

    @Test
    void historyIsLoadedOnlyForTheRequestedDifficulties() {
        historyRepository.saveAll(List.of(
                new CarryDifficultyHistory(first, 100, null, null, 1, JANUARY, FEBRUARY),
                new CarryDifficultyHistory(second, 200, null, null, 1, JANUARY, FEBRUARY)));

        List<CarryDifficultyHistory> history = historyRepository.findAllByCarryDifficulty_IdIn(Set.of(2L));

        assertEquals(1, history.size());
        assertEquals(200, history.getFirst().getPrice());
    }

    @Test
    void latestPeriodIsTheOneThatEndedLast() {
        historyRepository.saveAll(List.of(
                new CarryDifficultyHistory(first, 200, null, null, 1, FEBRUARY, MARCH),
                new CarryDifficultyHistory(first, 100, null, null, 1, JANUARY, FEBRUARY),
                new CarryDifficultyHistory(second, 300, null, null, 1, MARCH, MARCH.plusSeconds(60))));

        CarryDifficultyHistory latest = historyRepository.findFirstByCarryDifficultyOrderByDateToDesc(first).orElseThrow();

        assertEquals(MARCH, latest.getDateTo());
        assertEquals(200, latest.getPrice());
    }

    @Test
    void historyIsDeletedTogetherWithItsDifficulty() {
        historyRepository.save(new CarryDifficultyHistory(first, 100, null, null, 1, JANUARY, FEBRUARY));
        entityManager.flush();

        jdbcTemplate.update("delete from \"dungeon-hub\".carry_difficulty where id = 1");

        assertEquals(0, jdbcTemplate.queryForObject(
                "select count(*) from \"dungeon-hub\".carry_difficulty_history", Integer.class));
    }
}
