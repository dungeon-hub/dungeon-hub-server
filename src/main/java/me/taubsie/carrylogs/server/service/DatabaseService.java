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
    private static final String SCORE_SUFFIX = "-Score";
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
                activeConnection = dataSource.getConnection();
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

    public void logCarryInformation(CarryInformation carryInformation) throws SQLException {
        String sql = "INSERT INTO carries (carrier, player, amountOfCarries, carryDifficulty, carryType, " +
                "attachmentLink, time, approver) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        //TODO rework with new system
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, carryInformation.getCarrier());
            preparedStatement.setLong(2, carryInformation.getPlayer());
            preparedStatement.setLong(3, carryInformation.getAmountOfCarries());
            preparedStatement.setString(4, carryInformation.getCarryDifficulty().getIdentifier());
            preparedStatement.setString(5, carryInformation.getCarryType().getIdentifier());
            preparedStatement.setString(6, carryInformation.getAttachmentLink());
            preparedStatement.setTimestamp(7, Timestamp.from(carryInformation.getTime()));
            if (carryInformation.getApprover() != null) {
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

        //TODO rework with new system
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, carryInformation.getCarrier());
            preparedStatement.setLong(3, carryInformation.getPlayer());
            preparedStatement.setLong(4, carryInformation.getAmountOfCarries());
            preparedStatement.setString(5, carryInformation.getCarryDifficulty().getIdentifier());
            preparedStatement.setString(6, carryInformation.getCarryType().getIdentifier());
            preparedStatement.setTimestamp(7, Timestamp.from(carryInformation.getTime()));
            preparedStatement.executeUpdate();
        }
    }

    public void addToApprovingQueue(Long id, CarryInformation carryInformation) throws SQLException {
        String sql = "INSERT INTO log_approving_queue (id, carrier, player, amountOfCarries, carryDifficulty, " +
                "carryType, attachmentLink, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        //TODO rework with new system
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, carryInformation.getCarrier());
            preparedStatement.setLong(4, carryInformation.getAmountOfCarries());
            preparedStatement.setLong(3, carryInformation.getPlayer());
            preparedStatement.setString(5, carryInformation.getCarryDifficulty().getIdentifier());
            preparedStatement.setString(6, carryInformation.getCarryType().getIdentifier());
            preparedStatement.setString(7, carryInformation.getAttachmentLink());
            preparedStatement.setTimestamp(8, Timestamp.from(carryInformation.getTime()));
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
        String sql = "select time, amountOfCarries, carryDifficulty, carryType, player, carrier, attachmentLink from " +
                "log_approving_queue where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                CarryDifficulty carryDifficulty = loadCarryDifficulty(resultSet.getLong(3)).orElse(null);

                result.add(new CarryInformation(
                        resultSet.getTimestamp(1).toInstant(),
                        resultSet.getLong(2),
                        carryDifficulty,
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                CarryDifficulty carryDifficulty = loadCarryDifficulty(resultSet.getLong(3)).orElse(null);

                result.add(new CarryInformation(
                        resultSet.getTimestamp(1).toInstant(),
                        resultSet.getLong(2),
                        carryDifficulty,
                        resultSet.getLong(5),
                        resultSet.getLong(6)
                ));
            }
        }

        return result;
    }

    public Map<Long, List<CarryInformation>> getApprovingQueue() throws SQLException {
        String sql = "select time, amountOfCarries, carryDifficulty, carryType, player, carrier, attachmentLink, id " +
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
                        resultSet.getLong(5),
                        resultSet.getLong(6)
                );
                carryInformation.setAttachmentLink(resultSet.getString(7));

                long id = resultSet.getLong(8);

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
        String sql = "select time, amountOfCarries, carryDifficulty, carryType, player, carrier, id " +
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
                        resultSet.getLong(5),
                        resultSet.getLong(6)
                );

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

    public Map<String, Long> countScoreForCarrier(long serverId, long carrierId) throws SQLException {
        Map<String, Long> scoreMap = new HashMap<>();

        for(CarryType carryType : loadCarryTypesForServer(serverId)) {
            scoreMap.put(carryType.getDisplayName() + SCORE_SUFFIX, countScoreForCarrier(carrierId, carryType));
            scoreMap.put("Alltime-" + carryType.getDisplayName() + SCORE_SUFFIX, countAlltimeScoreForCarrier(carrierId,
                    carryType));
            scoreMap.put("Event-" + carryType.getDisplayName() + SCORE_SUFFIX, countEventScoreForCarrier(carrierId,
                    carryType));
        }

        return scoreMap;
    }

    public Map<Long, Long> getLeaderboard(int page, CarryType carryType, LeaderboardType leaderboardType) throws SQLException {
        String table = switch (leaderboardType) {
            case NORMAL -> "score";
            case EVENT -> "event_score";
            case ALLTIME -> "alltime_score";
        };

        String sql = "select id, score from " + table + " where score > 0 and carry-type = ? order by score DESC " +
                "limit 10 offset " + CarryLogService.getInstance().getOffsetFromPageNumber(page);

        return getLeaderboard(sql, carryType);
    }

    public Long getLeaderboardEntries(String type) throws SQLException {
        String sql = switch (type.toLowerCase()) {
            case "dungeons" -> "select count(*) from dungeon_score where score > 0";
            case "slayer" -> "select count(*) from slayer_score where score > 0";
            case "kuudra" -> "select count(*) from kuudra_score where score > 0";
            case "alltime-dungeons" -> "select count(*) from alltime_dungeon_score where score > 0";
            case "alltime-slayer" -> "select count(*) from alltime_slayer_score where score > 0";
            case "alltime-kuudra" -> "select count(*) from alltime_kuudra_score where score > 0";
            case "event-slayer" -> "select count(*) from event_slayer_score where score > 0";
            case "event-dungeons" -> "select count(*) from event_dungeon_score where score > 0";
            default -> "";
        };

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        }

        return 0L;
    }

    public Long getLeaderboardPages(String type) throws SQLException {
        Long entries = getLeaderboardEntries(type);

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

    public long updateEventScore(long carrierId, long amount, @NotNull CarryType carryType) throws SQLException {
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

            return newScore;
        }
    }

    public long updateLifetimeScore(long carrierId, long amount, @NotNull CarryType carryType) throws SQLException {
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

            return newScore;
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
        return roleMap.entrySet().stream().map(carryRole -> carryRole.getKey().name() + " = " + carryRole.getValue()).collect(Collectors.joining(", "));
    }

    public Map<Long, Long> getUsersWithLessScore(String type, long score) throws SQLException {
        Map<Long, Long> result = new HashMap<>();
        String sql = switch (type.toLowerCase()) {
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

        if (sql.isEmpty()) {
            return result;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, score);

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