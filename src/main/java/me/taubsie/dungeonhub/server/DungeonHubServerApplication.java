package me.taubsie.dungeonhub.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import me.taubsie.dungeonhub.server.config.ConfigService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

//TODO fix the api not showing the correct data types -> use UpdateModel instead of ModifyModel
@SpringBootApplication
@EntityScan("me.taubsie.dungeonhub.*")
@OpenAPIDefinition(info = @Info(title = "Dungeon Hub API", version = "v1", contact = @Contact(name = "Dungeon Hub",
        url = "discord.dungeon-hub.net", email = "contact@dungeon-hub.net"), termsOfService = "https://dungeon-hub" +
        ".net/terms-of-service", extensions = {@Extension(name = "x-logo", properties = {@ExtensionProperty(name =
        "url", value = "/cdn/static/favicon.gif"), @ExtensionProperty(name = "altText", value = "Dungeon Hub Logo"),
        @ExtensionProperty(name = "href", value = "#")})}), servers = {@Server(url = "https://api.dungeon-hub.net/")})
public class DungeonHubServerApplication {
    public static void main(String[] args) {
        ConfigService.ensureConfigFile();

        SpringApplication.run(DungeonHubServerApplication.class, args);
    }
}