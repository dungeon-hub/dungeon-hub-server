package me.taubsie.carrylogs.server.service;

import me.taubsie.carrylogs.CarryInformation;
import me.taubsie.carrylogs.CarryRole;
import me.taubsie.carrylogs.config.ConfigProperty;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class DatabaseService {
    private static DatabaseService instance;
    private final MariaDbDataSource dataSource;
    private final Connection connection;

    public DatabaseService() {
        dataSource = new MariaDbDataSource();
        Connection activeConnection = null;

        try {
            dataSource.setUrl("jdbc:mariadb://" + ConfigProperty.DATABASE_HOST + ":" + ConfigProperty.DATABASE_PORT + "/" + ConfigProperty.DATABASE_SCHEMA);

            dataSource.setUser(ConfigProperty.DATABASE_USER.getValue());
            dataSource.setPassword(ConfigProperty.DATABASE_PASSWORD.getValue());

            activeConnection = dataSource.getConnection();
        }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
        }

        connection = activeConnection;
    }

    public static DatabaseService getInstance() {
        if(instance == null) {
            instance = new DatabaseService();
        }

        return instance;
    }

    public boolean hasInvalidConfigValues() {
        ConfigProperty[] configProperties = new ConfigProperty[]{
                ConfigProperty.DATABASE_HOST,
                ConfigProperty.DATABASE_PASSWORD,
                ConfigProperty.DATABASE_USER,
                ConfigProperty.DATABASE_PORT,
                ConfigProperty.DATABASE_SCHEMA};

        if(Arrays.stream(configProperties).anyMatch(configProperty -> configProperty.getValue() == null)) {
            return true;
        }

        try {
            return Integer.parseInt(ConfigProperty.DATABASE_PORT.getValue()) <= 0;
        }
        catch(NumberFormatException numberFormatException) {
            return true;
        }
    }

    public DataSource getDataSource() {
        if(hasInvalidConfigValues()) {
            return null;
        }

        return dataSource;
    }

    public void logCarryInformation(CarryInformation carryInformation) throws SQLException {
        String sql = "INSERT INTO carries (carrier, player, amountOfCarries, carryDifficulty, carryType, " +
                "attachmentLink, time, approver) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carryInformation.getCarrier());
            preparedStatement.setLong(2, carryInformation.getPlayer());
            preparedStatement.setLong(3, carryInformation.getAmountOfCarries());
            preparedStatement.setString(4, carryInformation.getCarryDifficulty());
            preparedStatement.setString(5, carryInformation.getCarryType());
            preparedStatement.setString(6, carryInformation.getAttachmentLink());
            preparedStatement.setTimestamp(7, Timestamp.from(carryInformation.getTime()));
            if(carryInformation.getApprover() != null) {
                preparedStatement.setLong(8, carryInformation.getApprover());
            } else {
                preparedStatement.setNull(8, Types.BIGINT);
            }
            preparedStatement.executeUpdate();
        }
    }

    public void addToLogQueue(Long id, CarryInformation carryInformation) throws SQLException {
        String sql = "INSERT INTO log_queue (id, carrier, player, amountOfCarries, carryDifficulty, carryType, time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, carryInformation.getCarrier());
            preparedStatement.setLong(3, carryInformation.getPlayer());
            preparedStatement.setLong(4, carryInformation.getAmountOfCarries());
            preparedStatement.setString(5, carryInformation.getCarryDifficulty());
            preparedStatement.setString(6, carryInformation.getCarryType());
            preparedStatement.setTimestamp(7, Timestamp.from(carryInformation.getTime()));
            preparedStatement.executeUpdate();
        }
    }

    public void addToApprovingQueue(Long id, CarryInformation carryInformation) throws SQLException {
        String sql = "INSERT INTO log_approving_queue (id, carrier, player, amountOfCarries, carryDifficulty, " +
                "carryType, attachmentLink, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, carryInformation.getCarrier());
            preparedStatement.setLong(4, carryInformation.getAmountOfCarries());
            preparedStatement.setLong(3, carryInformation.getPlayer());
            preparedStatement.setString(5, carryInformation.getCarryDifficulty());
            preparedStatement.setString(6, carryInformation.getCarryType());
            preparedStatement.setString(7, carryInformation.getAttachmentLink());
            preparedStatement.setTimestamp(8, Timestamp.from(carryInformation.getTime()));
            preparedStatement.executeUpdate();
        }
    }

    public void removeFromLogQueue(Long id) throws SQLException {
        String sql = "DELETE from log_queue where id = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }

    public void removeFromApprovingQueue(Long id) throws SQLException {
        String sql = "DELETE from log_approving_queue where id = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }

    public Set<CarryInformation> getFromApprovingQueue(Long id) throws SQLException {
        Set<CarryInformation> result = new HashSet<>();
        String sql = "select time, amountOfCarries, carryDifficulty, carryType, player, carrier, attachmentLink from " +
                "log_approving_queue where id = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                result.add(new CarryInformation(
                        resultSet.getTimestamp(1).toInstant(),
                        resultSet.getLong(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getLong(5),
                        resultSet.getLong(6),
                        resultSet.getString(7)
                ));
            }
        }

        return result;
    }

    public Set<CarryInformation> getFromLogQueue(Long id) throws SQLException {
        Set<CarryInformation> result = new HashSet<>();
        String sql = "select time, amountOfCarries, carryDifficulty, carryType, player, carrier from log_queue where " +
                "id = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                result.add(new CarryInformation(
                        resultSet.getTimestamp(1).toInstant(),
                        resultSet.getLong(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getLong(5),
                        resultSet.getLong(6)
                ));
            }
        }

        return result;
    }

    public Map<Long, List<CarryInformation>> getApprovingQueue() {
        //TODO implement -> in a hashmap, one key can only have one value
        return new HashMap<>();
    }

    public Map<Long, List<CarryInformation>> getLogQueue() {
        //TODO implement -> in a hashmap, one key can only have one value
        return new HashMap<>();
    }

    public Map<String, Long> countScoreForCarrier(long carrierId) throws SQLException {
        Map<String, Long> scoreMap = new HashMap<>();

        scoreMap.put("dungeon", countDungeonScoreForCarrier(carrierId));
        scoreMap.put("slayer", countSlayerScoreForCarrier(carrierId));
        scoreMap.put("kuudra", countKuudraScoreForCarrier(carrierId));

        return scoreMap;
    }

    public long countDungeonScoreForCarrier(long carrierId) throws SQLException {
        String sql = "select score from dungeon_score where id = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carrierId);

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next() ? resultSet.getLong(1) : 0L;
        }
    }

    public long countKuudraScoreForCarrier(long carrierId) throws SQLException {
        String sql = "select score from kuudra_score where id = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carrierId);

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next() ? resultSet.getLong(1) : 0L;
        }
    }

    public long countSlayerScoreForCarrier(long carrierId) throws SQLException {
        String sql = "select score from slayer_score where id = ?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carrierId);

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next() ? resultSet.getLong(1) : 0L;
        }
    }

    public Map<Long, Long> getDungeonLeaderboard() throws SQLException {
        String sql = "select id, score from dungeon_score where score > 0 order by score DESC limit 10";

        return getLeaderboard(sql);
    }

    public Map<Long, Long> getSlayerLeaderboard() throws SQLException {
        String sql = "select id, score from slayer_score where score > 0 order by score DESC limit 10";

        return getLeaderboard(sql);
    }

    public Map<Long, Long> getKuudraLeaderboard() throws SQLException {
        String sql = "select id, score from kuudra_score where score > 0 order by score DESC limit 10";

        return getLeaderboard(sql);
    }

    private Map<Long, Long> getLeaderboard(String sql) throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            Map<Long, Long> result = new LinkedHashMap<>();
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                result.put(resultSet.getLong(1), resultSet.getLong(2));
            }

            return result;
        }
    }

    public long updateDungeonScore(long carrierId, long amount) throws SQLException {
        return updateScore(carrierId, amount, "dungeons");
    }

    public long updateKuudraScore(long carrierId, long amount) throws SQLException {
        return updateScore(carrierId, amount, "kuudra");
    }

    public long updateSlayerScore(long carrierId, long amount) throws SQLException {
        return updateScore(carrierId, amount, "slayer");
    }

    public long updateScore(long carrierId, long amount, String type) throws SQLException {
        String firstSql = switch(type.toLowerCase()) {
            case "dungeon", "dungeons" -> "SELECT score from dungeon_score where id = ?";
            case "kuudra" -> "SELECT score from kuudra_score where id = ?";
            case "slayer" -> "SELECT score from slayer_score where id = ?";
            default -> "";
        };

        String secondSql = switch(type.toLowerCase()) {
            case "dungeon", "dungeons" -> "INSERT INTO dungeon_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            case "kuudra" -> "INSERT INTO kuudra_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            case "slayer" -> "INSERT INTO slayer_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            default -> "";
        };

        if(firstSql.isEmpty() || secondSql.isEmpty()) {
            return 0L;
        }

        try(PreparedStatement firstStatement = connection.prepareStatement(firstSql);
            PreparedStatement secondStatement = connection.prepareStatement(secondSql)) {
            firstStatement.setLong(1, carrierId);

            ResultSet resultSet = firstStatement.executeQuery();
            long newScore = resultSet.next() ? resultSet.getLong(1) : 0L;
            newScore += amount;

            newScore = (newScore < 0) ? 0L : newScore;

            secondStatement.setLong(1, carrierId);
            secondStatement.setLong(2, newScore);
            secondStatement.setLong(3, newScore);
            secondStatement.executeUpdate();

            return newScore;
        }
    }

    public void addRoles(long id, List<CarryRole> roles) throws SQLException {
        //TODO implement
    }
}