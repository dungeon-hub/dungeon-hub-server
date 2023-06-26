package me.taubsie.carrylogs.server.service;

import me.taubsie.carrylogs.server.exceptions.ForbiddenException;
import me.taubsie.dungeonhub.common.*;
import me.taubsie.dungeonhub.common.config.ConfigProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.mariadb.jdbc.MariaDbDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static DatabaseService instance;
    private final MariaDbDataSource dataSource;
    private final Map<Long, Map<OldCarryRole, Boolean>> carrierMap = new HashMap<>();
    private Connection connection;

    public DatabaseService() {
        dataSource = new MariaDbDataSource();

        try {
            dataSource.setUrl("jdbc:mariadb://" + ConfigProperty.DATABASE_HOST + ":" + ConfigProperty.DATABASE_PORT + "/" + ConfigProperty.DATABASE_SCHEMA);

            dataSource.setUser(ConfigProperty.DATABASE_USER.getValue());
            dataSource.setPassword(ConfigProperty.DATABASE_PASSWORD.getValue());

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
            }, 0L, 1000L * 60 * 5);
        }
        catch (SQLException sqlException) {
            logger.error("Error during startup of database service.", sqlException);
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }

        return instance;
    }

    private void reloadConnection() throws SQLException {
        if (connection == null || !connection.isValid(5)) {
            if (connection != null) {
                connection.close();
            }

            Connection activeConnection = null;

            try {
                activeConnection = getDataSource().getConnection();
            }
            catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }

            connection = activeConnection;
        }

        if (connection != null) {
            reloadRoles();
        }
    }

    public boolean hasInvalidConfigValues() {
        ConfigProperty[] configProperties = new ConfigProperty[]{
                ConfigProperty.DATABASE_HOST,
                ConfigProperty.DATABASE_PASSWORD,
                ConfigProperty.DATABASE_USER,
                ConfigProperty.DATABASE_PORT,
                ConfigProperty.DATABASE_SCHEMA};

        if (Arrays.stream(configProperties).anyMatch(configProperty -> configProperty.getValue() == null)) {
            return true;
        }

        try {
            return Integer.parseInt(ConfigProperty.DATABASE_PORT.getValue()) <= 0;
        }
        catch (NumberFormatException numberFormatException) {
            return true;
        }
    }

    public DataSource getDataSource() {
        if (hasInvalidConfigValues()) {
            return null;
        }

        return dataSource;
    }

    //TODO once the bot is updated, remove the autorities and users table from the carrylogs schema
    public DataSource getApiDataSource() {
        if (hasInvalidConfigValues()) {
            return null;
        }

        MariaDbDataSource result = new MariaDbDataSource();

        try {
            String schema =
                    ConfigProperty.DATABASE_SCHEMA.getValue() == null || ConfigProperty.DATABASE_API_SCHEMA.getValue().isBlank()
                            ? ConfigProperty.DATABASE_SCHEMA.getValue()
                            : ConfigProperty.DATABASE_API_SCHEMA.getValue();

            result.setUrl("jdbc:mariadb://" + ConfigProperty.DATABASE_HOST + ":" + ConfigProperty.DATABASE_PORT + "/" + schema);

            result.setUser(ConfigProperty.DATABASE_USER.getValue());
            result.setPassword(ConfigProperty.DATABASE_PASSWORD.getValue());
        }
        catch (SQLException sqlException) {
            logger.error("Error during startup of database service.", sqlException);
        }

        return result;
    }

    private void reloadRoles() {
        carrierMap.clear();

        String sql = "SELECT * FROM carrier";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                Map<OldCarryRole, Boolean> roleMap = new EnumMap<>(OldCarryRole.class);
                for(OldCarryRole oldCarryRole : OldCarryRole.values()) {
                    roleMap.put(oldCarryRole, resultSet.getBoolean(oldCarryRole.name()));
                }

                carrierMap.put(resultSet.getLong(1), roleMap);
            }
        }
        catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void logCarryInformation(CarryInformation carryInformation) throws SQLException {
        String sql = "INSERT INTO carries (carrier, player, amount, carry_difficulty, attachment_link, time, " +
                "approver) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carryInformation.getCarrier());
            preparedStatement.setLong(2, carryInformation.getPlayer());
            preparedStatement.setLong(3, carryInformation.getAmountOfCarries());
            preparedStatement.setLong(4, carryInformation.getCarryDifficulty().getId());
            preparedStatement.setString(5, carryInformation.getAttachmentLink());
            preparedStatement.setTimestamp(6, Timestamp.from(carryInformation.getTime()));
            if (carryInformation.getApprover() != null) {
                preparedStatement.setLong(7, carryInformation.getApprover());
            } else {
                preparedStatement.setNull(7, Types.BIGINT);
            }
            preparedStatement.executeUpdate();
        }
    }

    public void addToLogQueue(Long id, CarryInformation carryInformation) throws SQLException {
        String sql = "INSERT INTO log_queue (id, carrier, player, amount, carry_difficulty, time) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, carryInformation.getCarrier());
            preparedStatement.setLong(3, carryInformation.getPlayer());
            preparedStatement.setLong(4, carryInformation.getAmountOfCarries());
            preparedStatement.setLong(5, carryInformation.getCarryDifficulty().getId());
            preparedStatement.setTimestamp(6, Timestamp.from(carryInformation.getTime()));
            preparedStatement.executeUpdate();
        }
    }

    public void addToApprovingQueue(Long id, CarryInformation carryInformation) throws SQLException {
        String sql = "INSERT INTO log_approving_queue (id, carrier, player, amount, carry_difficulty, " +
                "attachment_link, time) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, carryInformation.getCarrier());
            preparedStatement.setLong(3, carryInformation.getPlayer());
            preparedStatement.setLong(4, carryInformation.getAmountOfCarries());
            preparedStatement.setLong(5, carryInformation.getCarryDifficulty().getId());
            preparedStatement.setString(6, carryInformation.getAttachmentLink());
            preparedStatement.setTimestamp(7, Timestamp.from(carryInformation.getTime()));
            preparedStatement.executeUpdate();
        }
    }

    public void removeFromLogQueue(Long id) throws SQLException {
        String sql = "DELETE from log_queue where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }

    public void removeFromApprovingQueue(Long id) throws SQLException {
        String sql = "DELETE from log_approving_queue where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }
    }

    public Set<CarryInformation> getFromApprovingQueue(Long id) throws SQLException {
        Set<CarryInformation> result = new HashSet<>();
        String sql = "select time, amount, carry_difficulty, player, carrier, attachment_link " +
                "from log_approving_queue where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                CarryDifficulty carryDifficulty = loadCarryDifficulty(resultSet.getLong(3)).orElse(null);

                result.add(new CarryInformation(
                        resultSet.getTimestamp(1).toInstant(),
                        resultSet.getLong(2),
                        carryDifficulty,
                        resultSet.getLong(4),
                        resultSet.getLong(5),
                        resultSet.getString(6)
                ));
            }
        }

        return result;
    }

    public Set<CarryInformation> getFromLogQueue(Long id) throws SQLException {
        Set<CarryInformation> result = new HashSet<>();
        String sql = "select time, amount, carry_difficulty, player, carrier from log_queue where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                CarryDifficulty carryDifficulty = loadCarryDifficulty(resultSet.getLong(3)).orElse(null);

                result.add(new CarryInformation(
                        resultSet.getTimestamp(1).toInstant(),
                        resultSet.getLong(2),
                        carryDifficulty,
                        resultSet.getLong(4),
                        resultSet.getLong(5)
                ));
            }
        }

        return result;
    }

    public Map<Long, List<CarryInformation>> getApprovingQueue() throws SQLException {
        String sql = "select time, amount, carry_difficulty, player, carrier, attachment_link, id " +
                "from log_approving_queue";

        Map<Long, List<CarryInformation>> result = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                CarryDifficulty carryDifficulty = loadCarryDifficulty(resultSet.getLong(3)).orElse(null);

                CarryInformation carryInformation = new CarryInformation(
                        resultSet.getTimestamp(1).toInstant(),
                        resultSet.getLong(2),
                        carryDifficulty,
                        resultSet.getLong(4),
                        resultSet.getLong(5)
                );
                carryInformation.setAttachmentLink(resultSet.getString(6));

                long id = resultSet.getLong(7);

                if (result.containsKey(id)) {
                    result.get(id).add(carryInformation);
                } else {
                    result.put(id, new ArrayList<>(List.of(carryInformation)));
                }
            }
        }

        return result;
    }

    public Map<Long, List<CarryInformation>> getLogQueue() throws SQLException {
        String sql = "select time, amount, carry_difficulty, player, carrier, id " +
                "from log_queue";

        Map<Long, List<CarryInformation>> result = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                CarryDifficulty carryDifficulty = loadCarryDifficulty(resultSet.getLong(3)).orElse(null);

                CarryInformation carryInformation = new CarryInformation(
                        resultSet.getTimestamp(1).toInstant(),
                        resultSet.getLong(2),
                        carryDifficulty,
                        resultSet.getLong(4),
                        resultSet.getLong(5)
                );

                long id = resultSet.getLong(6);

                if (result.containsKey(id)) {
                    result.get(id).add(carryInformation);
                } else {
                    result.put(id, new ArrayList<>(List.of(carryInformation)));
                }
            }
        }

        return result;
    }

    public long countScoreForCarrier(long carrierId, CarryType carryType, ScoreType scoreType) throws SQLException {
        return switch (scoreType) {
            case DEFAULT -> countScoreForCarrier(carrierId, carryType);
            case ALLTIME -> countAlltimeScoreForCarrier(carrierId, carryType);
            case EVENT -> countEventScoreForCarrier(carrierId, carryType);
        };
    }

    public long countScoreForCarrier(long carrierId, CarryType carryType) throws SQLException {
        String sql = "select score from score where id = ? and carry_type = ?";

        return countScore(sql, carrierId, carryType);
    }

    public long countAlltimeScoreForCarrier(long carrierId, CarryType carryType) throws SQLException {
        String sql = "select score from alltime_score where id = ? and carry_type = ?";

        return countScore(sql, carrierId, carryType);
    }

    public long countEventScoreForCarrier(long carrierId, CarryType carryType) throws SQLException {
        String sql = "select score from event_score where id = ? and carry_type = ?";

        return countScore(sql, carrierId, carryType);
    }

    private long countScore(@NotNull String sql, long carrierId, CarryType carryType) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carrierId);
            preparedStatement.setLong(2, carryType.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next() ? resultSet.getLong(1) : 0L;
        }
    }

    public List<ScoreValue> countScoreForCarrier(long serverId, long carrierId) throws SQLException {
        List<ScoreValue> scoreValues = new ArrayList<>();

        for(CarryType carryType : loadCarryTypesForServer(serverId)) {
            for(ScoreType scoreType : ScoreType.values()) {
                scoreValues.add(new ScoreValue(carryType, scoreType, countScoreForCarrier(carrierId, carryType, scoreType)));
            }
        }

        return scoreValues;
    }

    public Map<Long, Long> getLeaderboard(int page, CarryType carryType, ScoreType scoreType) throws SQLException {
        String table = switch (scoreType) {
            case DEFAULT -> "score";
            case EVENT -> "event_score";
            case ALLTIME -> "alltime_score";
        };

        String sql = "select id, score from " + table + " where score > 0 and carry_type = ? order by score DESC " +
                "limit 10 offset " + CarryLogService.getInstance().getOffsetFromPageNumber(page);

        return getLeaderboard(sql, carryType);
    }

    public Long getLeaderboardEntries(CarryType carryType, ScoreType scoreType) throws SQLException {
        String table = switch (scoreType) {
            case DEFAULT -> "score";
            case EVENT -> "event_score";
            case ALLTIME -> "alltime_score";
        };

        String sql = "select count(*) from " + table + " where score > 0 and carry_type = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carryType.getId());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        }

        return 0L;
    }

    public Long getLeaderboardPages(CarryType carryType, ScoreType scoreType) throws SQLException {
        Long entries = getLeaderboardEntries(carryType, scoreType);

        if (entries == null || entries <= 0) {
            entries = 1L;
        }

        return Math.round(Math.ceil(entries / 10.0));
    }

    private Map<Long, Long> getLeaderboard(String sql, CarryType carryType) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carryType.getId());

            Map<Long, Long> result = new LinkedHashMap<>();
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                result.put(resultSet.getLong(1), resultSet.getLong(2));
            }

            return result;
        }
    }

    public long updateScore(long carrierId, long amount, @NotNull CarryType carryType) throws SQLException {
        String firstSql = "SELECT score from score where carry_type = ? and id = ?";

        String secondSql = "INSERT INTO score (id, carry_type, score) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE score " +
                "= ?";

        updateLifetimeScore(carrierId, amount, carryType);

        updateEventScore(carrierId, amount, carryType);

        try (PreparedStatement firstStatement = connection.prepareStatement(firstSql);
             PreparedStatement secondStatement = connection.prepareStatement(secondSql)) {
            firstStatement.setLong(1, carryType.getId());
            firstStatement.setLong(2, carrierId);

            ResultSet resultSet = firstStatement.executeQuery();
            long newScore = resultSet.next() ? resultSet.getLong(1) : 0L;
            newScore += amount;
            newScore = (newScore < 0) ? 0L : newScore;

            secondStatement.setLong(1, carrierId);
            secondStatement.setLong(2, carryType.getId());
            secondStatement.setLong(3, newScore);
            secondStatement.setLong(4, newScore);
            secondStatement.executeUpdate();

            return newScore;
        }
    }

    public void updateEventScore(long carrierId, long amount, @NotNull CarryType carryType) throws SQLException {
        String firstSql = "SELECT score from event_score where carry_type = ? and id = ?";

        String secondSql = "INSERT INTO event_score (id, carry_type, score) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "score = ?";

        try (PreparedStatement firstStatement = connection.prepareStatement(firstSql);
             PreparedStatement secondStatement = connection.prepareStatement(secondSql)) {
            firstStatement.setLong(1, carryType.getId());
            firstStatement.setLong(2, carrierId);

            ResultSet resultSet = firstStatement.executeQuery();
            long newScore = resultSet.next() ? resultSet.getLong(1) : 0L;
            newScore += amount;

            newScore = (newScore >= 0) ? newScore : 0L;

            secondStatement.setLong(1, carrierId);
            secondStatement.setLong(2, carryType.getId());
            secondStatement.setLong(3, newScore);
            secondStatement.setLong(4, newScore);
            secondStatement.executeUpdate();
        }
    }

    public void updateLifetimeScore(long carrierId, long amount, @NotNull CarryType carryType) throws SQLException {
        String firstSql = "SELECT score from alltime_score where carry_type = ? and id = ?";

        String secondSql = "INSERT INTO alltime_score (id, carry_type, score) VALUES (?, ?, ?) ON DUPLICATE KEY " +
                "UPDATE score = ?";

        try (PreparedStatement firstStatement = connection.prepareStatement(firstSql);
             PreparedStatement secondStatement = connection.prepareStatement(secondSql)) {
            firstStatement.setLong(1, carryType.getId());
            firstStatement.setLong(2, carrierId);

            ResultSet resultSet = firstStatement.executeQuery();
            long newScore = resultSet.next() ? resultSet.getLong(1) : 0L;
            newScore += amount;

            newScore = (newScore >= 0) ? newScore : 0L;

            secondStatement.setLong(1, carrierId);
            secondStatement.setLong(2, carryType.getId());
            secondStatement.setLong(3, newScore);
            secondStatement.setLong(4, newScore);
            secondStatement.executeUpdate();
        }
    }

    public void addRoles(long id, List<OldCarryRole> roles) throws SQLException {
        Map<OldCarryRole, Boolean> roleMap = new EnumMap<>(OldCarryRole.class);

        for(OldCarryRole oldCarryRole : OldCarryRole.values()) {
            roleMap.put(oldCarryRole, roles.contains(oldCarryRole));
        }

        if (carrierMap.containsKey(id) && carrierMap.get(id).equals(roleMap)) {
            return;
        }

        String sql = "INSERT INTO carrier(id, " + getKeys(roleMap) + ") VALUES (?, " + getValues(roleMap) + ") ON " +
                "DUPLICATE KEY UPDATE " + getKeysWithValues(roleMap);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();
        }

        carrierMap.put(id, roleMap);
    }

    //TODO rework and maybe put into mongodb
    public void addRoles(Map<Long, List<OldCarryRole>> roleData) throws SQLException {
        String sql = "INSERT INTO carrier(id, " +
                "F4, F5, F6, F7, MASTER_MODE, " +
                "EMAN_T3, EMAN_T4, BLAZE_T2, BLAZE_T3, BLAZE_T4, " +
                "BASIC, HOT, BURNING, FIERY, INFERNAL) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON " +
                "DUPLICATE KEY UPDATE " +
                "F4=?, F5=?, F6=?, F7=?, MASTER_MODE=?, " +
                "EMAN_T3=?, EMAN_T4=?, BLAZE_T2=?, BLAZE_T3=?, BLAZE_T4=?, " +
                "BASIC=?, HOT=?, BURNING=?, FIERY=?, INFERNAL=?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for(Map.Entry<Long, List<OldCarryRole>> roleEntry : roleData.entrySet()) {
                preparedStatement.setLong(1, roleEntry.getKey());

                for(int i = 0; i < OldCarryRole.values().length; i++) {
                    preparedStatement.setBoolean(i + 2, roleEntry.getValue().contains(OldCarryRole.values()[i]));
                    preparedStatement.setBoolean(OldCarryRole.values().length + i + 2,
                            roleEntry.getValue().contains(OldCarryRole.values()[i]));
                }

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        }
    }

    public void addUserIfNotExists(long id) throws SQLException {
        if (carrierMap.containsKey(id)) {
            return;
        }

        carrierMap.put(id, getEmptyRoleMap());

        String sql = "INSERT IGNORE INTO carrier(id) VALUES (?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            preparedStatement.executeUpdate();
        }
    }

    public Map<OldCarryRole, Boolean> getEmptyRoleMap() {
        Map<OldCarryRole, Boolean> emptyRoleMap = new EnumMap<>(OldCarryRole.class);

        for(OldCarryRole oldCarryRole : OldCarryRole.values()) {
            emptyRoleMap.put(oldCarryRole, false);
        }

        return emptyRoleMap;
    }

    private String getKeys(Map<OldCarryRole, Boolean> roleMap) {
        return roleMap.keySet().stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private String getValues(Map<OldCarryRole, Boolean> roleMap) {
        return roleMap.values().stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    private String getKeysWithValues(Map<OldCarryRole, Boolean> roleMap) {
        return roleMap.entrySet().stream()
                .map(carryRole -> carryRole.getKey().name() + " = " + carryRole.getValue())
                .collect(Collectors.joining(", "));
    }

    public Map<Long, Long> getUsersWithLessScore(CarryType carryType, long score) throws SQLException {
        Map<Long, Long> result = new HashMap<>();
        String sql = "SELECT carrier.id, score FROM carrier LEFT JOIN score ON carrier.id = score.id WHERE 1 " +
                "in (f4, f5, f6, f7, master_mode, eman_t3, eman_t4, blaze_t2, blaze_t3, blaze_t4, blaze_t3, hot, " +
                "burning, fiery, infernal) and (score < ? or score is null) and carry_type = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, score);
            preparedStatement.setLong(2, carryType.getId());

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                result.put(resultSet.getLong(1), resultSet.getLong(2));
            }
        }

        return result;
    }

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

            while(resultSet.next()) {
                strikes.add(StrikeData.fromResultSet(resultSet));
            }
        }

        return Collections.unmodifiableList(strikes);
    }

    public StrikeData insertStrikeData(StrikeData strikeData) throws SQLException, UnsupportedOperationException {
        if (strikeData.getId() != null) {
            throw new UnsupportedOperationException("Strike was already inserted - has an id");
        }

        String sql = "insert into strikes(serverId, user, striker, reason, time) VALUES (?, ?, ?, ?, ?)";

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

    public void removeStrike(long serverId, long id) throws SQLException, ForbiddenException {
        String firstSql = "select serverId from strikes where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(firstSql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                if (resultSet.getLong(1) != serverId) {
                    throw new ForbiddenException();
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

            while(resultSet.next()) {
                result.put(resultSet.getLong("id"), StrikeData.fromResultSet(resultSet));
            }
        }

        return result;
    }

    public List<CarryType> loadCarryTypes() throws SQLException {
        String sql = "select * from carry_type";

        List<CarryType> carryTypes = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                carryTypes.add(CarryType.fromResultSet(resultSet));
            }
        }

        return carryTypes;
    }

    public Map<Long, CarryType> loadCarryTypeMap() throws SQLException {
        Map<Long, CarryType> result = new HashMap<>();

        for(CarryType carryType : loadCarryTypes()) {
            result.put(carryType.getId(), carryType);
        }

        return result;
    }

    public List<CarryType> loadCarryTypesForServer(long serverId) throws SQLException {
        String sql = "select * from carry_type where server = ?";

        List<CarryType> carryTypes = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, serverId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                carryTypes.add(CarryType.fromResultSet(resultSet));
            }
        }

        return carryTypes;
    }

    public Optional<CarryType> createCarryType(long serverId, String identifier, String displayName) throws SQLException {
        String sql = "insert into carry_type(server, identifier, display_name) values (?, ?, ?)";

        if (getCarryType(serverId, identifier).isPresent()) {
            return Optional.empty();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, serverId);
            preparedStatement.setString(2, identifier);
            preparedStatement.setString(3, displayName);

            preparedStatement.executeUpdate();

            ResultSet keys = preparedStatement.getGeneratedKeys();

            if (keys.next() && keys.getLong(1) > 0L) {
                return getCarryType(keys.getLong(1));
            }
        }

        return Optional.empty();
    }

    public Optional<CarryType> deleteCarryType(long serverId, long carryTypeId) throws SQLException {
        String sql = "delete from carry_type where id = ? and server = ? returning *";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carryTypeId);
            preparedStatement.setLong(2, serverId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(CarryType.fromResultSet(resultSet));
            }
        }

        return Optional.empty();
    }

    public Optional<CarryType> updateCarryType(CarryType carryType) throws SQLException {
        String sql = "update carry_type set display_name = ?, log_channel = ?, leaderboard_channel = ?, event_active = ? where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, carryType.getDisplayName());

            Optional<Long> logChannel = carryType.getLogChannel();
            if (logChannel.isPresent()) {
                preparedStatement.setLong(2, logChannel.get());
            } else {
                preparedStatement.setNull(2, Types.BIGINT);
            }

            Optional<Long> leaderboardChannel = carryType.getLeaderboardChannel();
            if (leaderboardChannel.isPresent()) {
                preparedStatement.setLong(3, leaderboardChannel.get());
            } else {
                preparedStatement.setNull(3, Types.BIGINT);
            }

            preparedStatement.setBoolean(4, carryType.isEventActive());

            preparedStatement.setLong(5, carryType.getId());

            preparedStatement.executeUpdate();
        }

        return getCarryType(carryType.getId());
    }

    public Optional<CarryType> getCarryType(long id) throws SQLException {
        String sql = "select * from carry_type where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(CarryType.fromResultSet(resultSet));
            }
        }

        return Optional.empty();
    }

    public Optional<CarryType> getCarryType(long server, String identifier) throws SQLException {
        String sql = "select * from carry_type where server = ? and identifier = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, server);
            preparedStatement.setString(2, identifier);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(CarryType.fromResultSet(resultSet));
            }
        }

        return Optional.empty();
    }

    public Optional<CarryTier> updateCarryTier(CarryTier carryTier) throws SQLException {
        String sql = "update carry_tier set display_name = ?, thumbnail_url = ?, category = ?, descriptive_name = ?, " +
                "price_channel = ?, price_description = ? where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, carryTier.getDisplayName());

            Optional<String> thumbnailUrl = carryTier.getThumbnailUrl();
            if (thumbnailUrl.isPresent()) {
                preparedStatement.setString(2, thumbnailUrl.get());
            } else {
                preparedStatement.setNull(2, Types.VARCHAR);
            }

            Optional<Long> category = carryTier.getCategory();
            if (category.isPresent()) {
                preparedStatement.setLong(3, category.get());
            } else {
                preparedStatement.setNull(3, Types.BIGINT);
            }

            Optional<String> descriptiveName = carryTier.getActualDescriptiveName();
            if (descriptiveName.isPresent()) {
                preparedStatement.setString(4, descriptiveName.get());
            } else {
                preparedStatement.setNull(4, Types.VARCHAR);
            }

            Optional<Long> priceChannel = carryTier.getPriceChannel();
            if (priceChannel.isPresent()) {
                preparedStatement.setLong(5, priceChannel.get());
            } else {
                preparedStatement.setNull(5, Types.BIGINT);
            }

            Optional<String> priceDescription = carryTier.getPriceDescription();
            if(priceDescription.isPresent()) {
                preparedStatement.setString(6, priceDescription.get());
            } else {
                preparedStatement.setNull(6, Types.VARCHAR);
            }

            preparedStatement.setLong(7, carryTier.getId());

            preparedStatement.executeUpdate();
        }

        return getCarryTier(carryTier.getId());
    }

    public Optional<CarryTier> createCarryTier(CarryType carryType, String identifier, String displayName) throws SQLException {
        String sql = "insert into carry_tier(carry_type, identifier, display_name) values (?, ?, ?)";

        if (getCarryTier(carryType, identifier).isPresent()) {
            return Optional.empty();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, carryType.getId());
            preparedStatement.setString(2, identifier);
            preparedStatement.setString(3, displayName);

            preparedStatement.executeUpdate();

            ResultSet keys = preparedStatement.getGeneratedKeys();

            if (keys.next() && keys.getLong(1) > 0L) {
                return getCarryTier(keys.getLong(1));
            }
        }

        return Optional.empty();
    }

    public Optional<CarryTier> getCarryTier(long id) throws SQLException {
        String sql = "select * from carry_tier where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(CarryTier.fromResultSet(resultSet, loadCarryTypeMap()));
            }
        }

        return Optional.empty();
    }

    public Optional<CarryTier> getCarryTier(CarryType carryType, String identifier) throws SQLException {
        String sql = "select * from carry_tier where carry_type = ? and identifier = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carryType.getId());
            preparedStatement.setString(2, identifier);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(CarryTier.fromResultSet(resultSet, loadCarryTypeMap()));
            }
        }

        return Optional.empty();
    }

    public List<CarryTier> loadCarryTiers() throws SQLException {
        String sql = "select * from carry_tier";

        Map<Long, CarryType> carryTypes = loadCarryTypeMap();
        List<CarryTier> carryTiers = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                carryTiers.add(CarryTier.fromResultSet(resultSet, carryTypes));
            }
        }

        return carryTiers;
    }

    public Map<Long, CarryTier> loadCarryTierMap() throws SQLException {
        String sql = "select * from carry_tier";

        Map<Long, CarryType> carryTypes = loadCarryTypeMap();
        Map<Long, CarryTier> result = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                result.put(resultSet.getLong("id"), CarryTier.fromResultSet(resultSet, carryTypes));
            }
        }

        return result;
    }

    public List<CarryTier> loadCarryTiersOfCarryType(CarryType carryType) throws SQLException {
        return loadCarryTiers().stream()
                .filter(carryTier -> Objects.equals(carryTier.getCarryType().getId(), carryType.getId()))
                .toList();
    }

    public Optional<CarryTier> loadCarryTierFromCategory(long categoryId) throws SQLException {
        String sql = "select * from carry_tier where category = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, categoryId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(CarryTier.fromResultSet(resultSet, loadCarryTypeMap()));
            }
        }

        return Optional.empty();
    }

    public List<CarryDifficulty> loadCarryDifficulties() throws SQLException {
        String sql = "select * from carry_difficulty";

        Map<Long, CarryTier> carryTiers = loadCarryTierMap();
        List<CarryDifficulty> carryDifficulties = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                carryDifficulties.add(CarryDifficulty.fromResultSet(resultSet, carryTiers));
            }
        }

        return carryDifficulties;
    }

    public List<CarryDifficulty> loadCarryDifficultiesForServer(long serverId) throws SQLException {
        return loadCarryTypesForServer(serverId)
                .stream()
                .flatMap(carryType -> {
                    try {
                        return loadCarryTiersOfCarryType(carryType).stream();
                    }
                    catch (SQLException sqlException) {
                        logger.error(null, sqlException);
                        return Stream.empty();
                    }
                })
                .flatMap(carryTier -> {
                    try {
                        return loadCarryDifficultiesOfCarryTier(carryTier).stream();
                    }
                    catch (SQLException sqlException) {
                        logger.error(null, sqlException);
                        return Stream.empty();
                    }
                })
                .toList();
    }

    public List<CarryDifficulty> loadCarryDifficultiesOfCarryTier(CarryTier carryTier) throws SQLException {
        return loadCarryDifficulties()
                .stream().filter(carryDifficulty -> Objects.equals(carryDifficulty.getCarryTier().getId(),
                        carryTier.getId()))
                .toList();
    }

    public Optional<CarryDifficulty> loadCarryDifficulty(long id) throws SQLException {
        String sql = "select * from carry_difficulty where id = ?";
        Map<Long, CarryTier> carryTiers = loadCarryTierMap();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(CarryDifficulty.fromResultSet(resultSet, carryTiers));
            }
        }

        return Optional.empty();
    }
}