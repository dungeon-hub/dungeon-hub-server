package me.taubsie.dungeonhub.server.config;

import me.taubsie.dungeonhub.common.StrikeData;
import me.taubsie.dungeonhub.common.exceptions.ProgramStartException;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.mariadb.jdbc.MariaDbDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private final MariaDbDataSource dataSource;
    private final ConfigService configService;
    private Connection connection;

    @Autowired
    @Lazy
    public DatabaseConfig(ConfigService configService) {
        this.configService = configService;

        this.dataSource = new MariaDbDataSource();

        String url = "jdbc:mariadb://" + configService.getDatabaseHost() + ":" + configService.getDatabasePort() + "/";
        String user = configService.getDatabaseUser();
        String password = configService.getDatabasePassword();

        //Ensure that Flyway configures the database BEFORE the actual connection to the database opens
        Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:migrations")
                .defaultSchema("flyway")
                .load().migrate();

        try {
            dataSource.setUrl(url + configService.getDatabaseSchema());

            dataSource.setUser(user);
            dataSource.setPassword(password);

            reloadConnection();

            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        reloadConnection();
                    }
                    catch (SQLException sqlException) {
                        logger.error("Error during connection establishment.", sqlException);
                    }
                }
            }, 1000L * 60, 1000L * 60 * 5);
        }
        catch (SQLException sqlException) {
            logger.error("Error during startup of database service. Please make sure that the correct values are set " +
                            "in \"{}{}dungeon-hub{}config{}server_config.properties\".", System.getProperty("user" +
                            ".home"),
                    File.separator, File.separator, File.separator);

            throw new ProgramStartException(sqlException);
        }
    }

    private void reloadConnection() throws SQLException {
        if (connection == null || !connection.isValid(5)) {
            if (connection != null) {
                connection.close();
            }

            if (hasInvalidConfigValues()) {
                return;
            }

            connection = dataSource.getConnection();
        }
    }

    public boolean hasInvalidConfigValues() {
        try {
            return configService.getDatabaseHost().isBlank()
                    || configService.getDatabasePassword().isBlank()
                    || configService.getDatabaseUser().isBlank()
                    || configService.getDatabasePort() <= 0
                    || configService.getDatabaseSchema().isBlank();
        }
        catch (NullPointerException ignored) {
            return true;
        }
    }

    @Bean
    @Primary
    public DataSource getDataSource() {
        if (dataSource == null) {
            throw new ProgramStartException("Couldn't connect to the database. Please ensure that all values are set " +
                    "up correctly.");
        }

        return dataSource;
    }

    // TODO move everything below to its own service

    public Optional<StrikeData> getStrikeDataById(long id) throws SQLException {
        String sql = "select * from strikes where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(StrikeData.fromResultSet(resultSet));
            }
        }

        return Optional.empty();
    }

    public List<StrikeData> getValidStrikeData(long serverId, Long userId) throws SQLException {
        List<StrikeData> strikeData = new ArrayList<>(getAllStrikeData(serverId, userId));

        if (strikeData.size() >= 4) {
            return strikeData;
        }

        if (filterStrikesByTime(strikeData, 9) >= 3) {
            return strikeData;
        }

        if (filterStrikesByTime(strikeData, 6) >= 2) {
            return strikeData;
        }

        filterStrikesByTime(strikeData, 3);

        return strikeData;
    }

    //called by reference, thanks to groldi <3
    private int filterStrikesByTime(List<StrikeData> strikeData, int amount) {
        List<StrikeData> copied = new ArrayList<>(strikeData);
        strikeData.clear();
        strikeData.addAll(copied.stream()
                .filter(data -> data.getStrikeTime()
                        .isAfter(data.getStrikeTime()
                                .minus(amount * 30L, TimeUnit.DAYS.toChronoUnit())))
                .toList());
        return strikeData.size();
    }

    public @Unmodifiable @NotNull List<StrikeData> getAllStrikeData(long serverId, long userId) throws SQLException {
        String sql = "select * from strikes where serverId = ? and user = ?";

        List<StrikeData> strikes = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, serverId);
            preparedStatement.setLong(2, userId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                strikes.add(StrikeData.fromResultSet(resultSet));
            }
        }

        return Collections.unmodifiableList(strikes);
    }

    public StrikeData insertStrikeData(StrikeData strikeData) throws SQLException, UnsupportedOperationException {
        if (strikeData.getId() != null) {
            throw new UnsupportedOperationException("Strike was already inserted - has an id");
        }

        String sql = "insert into strikes(server_id, user, striker, reason, time) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, strikeData.getServer());
            preparedStatement.setLong(2, strikeData.getUser());
            if (strikeData.getStriker() != null) {
                preparedStatement.setLong(3, strikeData.getStriker());
            } else {
                preparedStatement.setNull(3, Types.BIGINT);
            }
            preparedStatement.setString(4, strikeData.getReason());
            preparedStatement.setTimestamp(5, Timestamp.from(strikeData.getStrikeTime()));

            preparedStatement.executeUpdate();

            ResultSet keys = preparedStatement.getGeneratedKeys();

            if (keys.next()) {
                return strikeData.setId(keys.getLong(1));
            }
        }

        return strikeData;
    }

    public void removeStrike(long serverId, long id) throws SQLException {
        String firstSql = "select serverId from strikes where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(firstSql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                if (resultSet.getLong(1) != serverId) {
                    throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
                }
            } else {
                return;
            }
        }

        String secondSql = "delete from strikes where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(secondSql)) {
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();
        }
    }

    public Map<Long, StrikeData> getStrikesInServer(Long serverId) throws SQLException {
        String sql = "select * from strikes where serverId = ?";

        Map<Long, StrikeData> result = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, serverId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                result.put(resultSet.getLong("id"), StrikeData.fromResultSet(resultSet));
            }
        }

        return result;
    }
}