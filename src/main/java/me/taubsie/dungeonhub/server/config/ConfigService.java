package me.taubsie.dungeonhub.server.config;

import lombok.Getter;
import me.taubsie.dungeonhub.server.DungeonHubServerApplication;
import me.taubsie.dungeonhub.server.exception.ProgramStartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Service
public class ConfigService {
    private final String dungeonHubDirectory;

    public ConfigService(@Value("dungeon-hub.directory") String dungeonHubDirectory) {
        this.dungeonHubDirectory = dungeonHubDirectory;
    }

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

                Files.createDirectories(configFile.getParent());
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