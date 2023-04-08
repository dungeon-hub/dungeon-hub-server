package me.taubsie.carrylogs.server.service;

import me.taubsie.dungeonhub.common.CarryInformation;
import me.taubsie.dungeonhub.common.CarryRole;
import me.taubsie.dungeonhub.common.config.ConfigProperty;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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
        } catch(SQLException sqlException) {
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
        } catch(NumberFormatException numberFormatException) {
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

    public Map<Long, Long> getAlltimeDungeonLeaderboard() throws SQLException {
        String sql = "select id, score from alltime_dungeon_score where score > 0 order by score DESC limit 10";

        return getLeaderboard(sql);
    }

    public Map<Long, Long> getSlayerLeaderboard() throws SQLException {
        String sql = "select id, score from slayer_score where score > 0 order by score DESC limit 10";

        return getLeaderboard(sql);
    }

    public Map<Long, Long> getAlltimeSlayerLeaderboard() throws SQLException {
        String sql = "select id, score from alltime_slayer_score where score > 0 order by score DESC limit 10";

        return getLeaderboard(sql);
    }

    public Map<Long, Long> getKuudraLeaderboard() throws SQLException {
        String sql = "select id, score from kuudra_score where score > 0 order by score DESC limit 10";

        return getLeaderboard(sql);
    }

    public Map<Long, Long> getAlltimeKuudraLeaderboard() throws SQLException {
        String sql = "select id, score from alltime_kuudra_score where score > 0 order by score DESC limit 10";

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
            case "dungeon", "dungeons" ->
                    "INSERT INTO dungeon_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            case "kuudra" -> "INSERT INTO kuudra_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            case "slayer" -> "INSERT INTO slayer_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            default -> "";
        };

        if(firstSql.isEmpty() || secondSql.isEmpty()) {
            return 0L;
        }

        updateLifetimeScore(carrierId, amount, type);

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

    public long updateLifetimeScore(long carrierId, long amount, String type) throws SQLException {
        String firstSql = switch(type.toLowerCase()) {
            case "dungeon", "dungeons" -> "SELECT score from alltime_dungeon_score where id = ?";
            case "kuudra" -> "SELECT score from alltime_kuudra_score where id = ?";
            case "slayer" -> "SELECT score from alltime_slayer_score where id = ?";
            default -> "";
        };

        String secondSql = switch(type.toLowerCase()) {
            case "dungeon", "dungeons" ->
                    "INSERT INTO alltime_dungeon_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            case "kuudra" ->
                    "INSERT INTO alltime_kuudra_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            case "slayer" ->
                    "INSERT INTO alltime_slayer_score (id, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
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

            newScore = (newScore >= 0) ? newScore : 0L;

            secondStatement.setLong(1, carrierId);
            secondStatement.setLong(2, newScore);
            secondStatement.setLong(3, newScore);
            secondStatement.executeUpdate();

            return newScore;
        }
    }

    public void addRoles(long id, List<CarryRole> roles) throws SQLException {
        Map<CarryRole, Boolean> roleMap = new HashMap<>();

        for(CarryRole carryRole : CarryRole.values()) {
            roleMap.put(carryRole, roles.contains(carryRole));
        }

        String sql = "INSERT INTO carrier(id, " + getKeys(roleMap) + ") VALUES (?, " + getValues(roleMap) + ") ON " +
                "DUPLICATE KEY UPDATE " + getKeysWithValues(roleMap);

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();
        }
    }

    public void addRoles(Map<Long, List<CarryRole>> roleData) throws SQLException {
        String sql = "INSERT INTO carrier(id, " +
                "F4, F5, F6, F7, MASTER_MODE, " +
                "EMAN_T3, EMAN_T4, BLAZE_T2, BLAZE_T3, BLAZE_T4, " +
                "BASIC, HOT, BURNING, FIERY, INFERNAL) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON " +
                "DUPLICATE KEY UPDATE " +
                "F4=?, F5=?, F6=?, F7=?, MASTER_MODE=?, " +
                "EMAN_T3=?, EMAN_T4=?, BLAZE_T2=?, BLAZE_T3=?, BLAZE_T4=?, " +
                "BASIC=?, HOT=?, BURNING=?, FIERY=?, INFERNAL=?";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for(Map.Entry<Long, List<CarryRole>> roleEntry : roleData.entrySet()) {
                preparedStatement.setLong(1, roleEntry.getKey());

                for(int i = 0; i < CarryRole.values().length; i++) {
                    preparedStatement.setBoolean(i + 2, roleEntry.getValue().contains(CarryRole.values()[i]));
                    preparedStatement.setBoolean(CarryRole.values().length + i + 2, roleEntry.getValue().contains(CarryRole.values()[i]));
                }

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        }
    }

    public void addUserIfNotExists(long id) throws SQLException {
        String sql = "INSERT IGNORE INTO carrier(id) VALUES (?)";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();
        }
    }

    private String getKeys(Map<CarryRole, Boolean> roleMap) {
        return roleMap.keySet().stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private String getValues(Map<CarryRole, Boolean> roleMap) {
        return roleMap.values().stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    private String getKeysWithValues(Map<CarryRole, Boolean> roleMap) {
        return roleMap.entrySet().stream().map(carryRole -> carryRole.getKey().name() + " = " + carryRole.getValue()).collect(Collectors.joining(", "));
    }

    public Map<Long, Long> getUsersWithLessScore(String type, long score) throws SQLException {
        Map<Long, Long> result = new HashMap<>();
        String sql = switch(type.toLowerCase()) {
            case "dungeon", "dungeons" ->
                    "SELECT carrier.id, score FROM carrier LEFT JOIN dungeon_score score ON carrier" +
                            ".id = score.id where (f4 = 1 or f5 = 1 or f6 = 1 or f7 = 1 or master_mode = 1) and " +
                            "(score < ? or score is null)";
            case "slayer" ->
                    "SELECT carrier.id, score FROM carrier LEFT JOIN slayer_score score ON carrier.id = score.id " +
                            "where (eman_t3 = 1 or eman_t4 = 1 or blaze_t2 = 1 or blaze_t3 = 1 or blaze_t4 = 1) and " +
                            "(score < ? or score is null)";
            case "kuudra" ->
                    "SELECT carrier.id, score FROM carrier LEFT JOIN kuudra_score score ON carrier.id = score.id " +
                            "where (basic = 1 or hot = 1 or burning = 1 or fiery = 1 or infernal = 1) and (score < ?" +
                            " or score is null)";
            default -> "";
        };

        if(sql.isEmpty()) {
            return result;
        }

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, score);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                result.put(resultSet.getLong(1), resultSet.getLong(2));
            }
        }

        return result;
    }
}