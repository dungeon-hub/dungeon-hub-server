package me.taubsie.carrylogs.server.service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import me.taubsie.dungeonhub.common.config.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigDatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigDatabaseService.class);

    private static ConfigDatabaseService instance;
    private final MongoClient mongoClient;

    public ConfigDatabaseService() {
        mongoClient = MongoClients.create(getMongoClientSettings());
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
        if (instance == null) {
            instance = new ConfigDatabaseService();
        }

        return instance;
    }

    private MongoDatabase getDatabase() {
        return mongoClient.getDatabase("dungeon-hub");
    }
}