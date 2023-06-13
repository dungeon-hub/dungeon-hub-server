package me.taubsie.carrylogs.server.service;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.taubsie.dungeonhub.common.CarryDifficulty;
import me.taubsie.dungeonhub.common.CarryTier;
import me.taubsie.dungeonhub.common.CarryType;
import me.taubsie.dungeonhub.common.config.ConfigProperty;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigDatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigDatabaseService.class);

    private static ConfigDatabaseService instance;
    private MongoClient mongoClient;

    public ConfigDatabaseService() {
        mongoClient = MongoClients.create(getMongoClientSettings());

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                reloadConnection();
            }
        }, 0L, 1000L * 60 * 60 * 6);
    }

    private static String getConnectionString() {
        return "mongodb+srv://"
                + ConfigProperty.CONFIG_USER.getValue()
                + ":"
                + ConfigProperty.CONFIG_PASSWORD.getValue()
                + "@"
                + ConfigProperty.CONFIG_HOST.getValue()
                + "/?retryWrites=true&w=majority";
    }

    private static MongoClientSettings getMongoClientSettings() {
        String connectionString = getConnectionString();

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
    }

    public static ConfigDatabaseService getInstance() {
        if(instance == null) {
            instance = new ConfigDatabaseService();
        }

        return instance;
    }

    private void reloadConnection() {
        MongoClient newClient = MongoClients.create(getMongoClientSettings());

        if(mongoClient != null) {
            mongoClient.close();
        }

        mongoClient = newClient;
    }

    private MongoDatabase getDatabase() {
        return mongoClient.getDatabase("dungeon-hub");
    }

    private MongoCollection<Document> getCarryTypeCollection() {
        return getDatabase().getCollection("carry-types");
    }

    private MongoCollection<Document> getCarryTierCollection() {
        return getDatabase().getCollection("carry-tiers");
    }

    private MongoCollection<Document> getCarryDifficultyCollection() {
        return getDatabase().getCollection("carry-difficulty");
    }

    public List<CarryType> loadCarryTypes() {
        List<CarryType> carryTypes = new ArrayList<>();

        getCarryTypeCollection().find().forEach(document ->
                carryTypes.add(new CarryType(
                        document.getObjectId("_id").toString(),
                        document.getString("identifier"),
                        document.getLong("server")
                ))
        );

        return carryTypes;
    }

    public List<CarryType> loadCarryTypesForServer(long serverId) {
        return loadCarryTypes().stream().filter(carryType -> carryType.getServer() == serverId).toList();
    }

    public List<CarryTier> loadCarryTiers() {
        List<CarryType> carryTypes = loadCarryTypes();
        List<CarryTier> carryTiers = new ArrayList<>();

        getCarryTierCollection().find().forEach(document ->
                carryTiers.add(new CarryTier(
                        document.getObjectId("_id").toString(),
                        document.getString("identifier"),
                        document.getString("display_name"),
                        carryTypes.stream()
                                .filter(carryTier -> carryTier.getId().equalsIgnoreCase(document.getObjectId("carry-type"
                                ).toString()))
                                .findFirst()
                                .orElse(null),
                        document.getList("roles", Long.TYPE)
                ))
        );

        return carryTiers;
    }

    public List<CarryTier> loadCarryTiersOfCarryType(CarryType carryType) {
        return loadCarryTiersOfCarryType(carryType.getId());
    }

    public List<CarryTier> loadCarryTiersOfCarryType(String id) {
        return loadCarryTiers().stream().filter(carryTier -> carryTier.getCarryType().getId().equalsIgnoreCase(id)).toList();
    }

    public List<CarryDifficulty> loadCarryDifficulties() {
        //TODO implement
        return new ArrayList<>();
    }

    public List<CarryDifficulty> loadCarryDifficultiesForServer(long serverId) {
        //TODO implement
        return new ArrayList<>();
    }

    public List<CarryDifficulty> loadCarryDifficultiesOfCarryTier(CarryTier carryTier) {
        //TODO implement
        return new ArrayList<>();
    }

    public List<CarryDifficulty> loadCarryDifficultiesOfCarryTier(String id) {
        //TODO implement
        return new ArrayList<>();
    }
}