package me.taubsie.dungeonhub.server;

import lombok.Getter;
import me.taubsie.dungeonhub.common.exceptions.ProgramStartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Configuration
@PropertySource(value = "file:${user.home}/dungeon-hub/config/server_config.properties")
public class ConfigService {
    @Value("${db.host}")
    private String databaseHost;

    @Value("${db.port}")
    private int databasePort;

    @Value("${db.schema}")
    private String databaseSchema;

    @Value("${db.api-schema}")
    private String databaseApiSchema;

    @Value("${db.user}")
    private String databaseUser;

    @Value("${db.password}")
    private String databasePassword;

    public static void ensureConfigFile() {
        Path configFile = Paths.get(System.getProperty("user.home"),
                File.separator,
                "dungeon-hub",
                File.separator,
                "config",
                File.separator,
                "server_config.properties");

        if (!configFile.toFile().exists()) {
            try (InputStream inputStream = DungeonHubServerApplication.class
                    .getClassLoader()
                    .getResourceAsStream("config/default_server_config.properties")) {
                if (inputStream == null) {
                    throw new ProgramStartException("The file \"config/default_server_config.properties\" is missing. "
                            + "Please contact the developer for more information.");
                }

                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                     BufferedWriter bufferedWriter = Files.newBufferedWriter(configFile)) {
                    bufferedReader.transferTo(bufferedWriter);
                }
            }
            catch (IOException ioException) {
                throw new ProgramStartException(ioException);
            }
        }
    }
}