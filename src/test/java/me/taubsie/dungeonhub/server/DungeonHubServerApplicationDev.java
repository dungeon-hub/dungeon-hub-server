package me.taubsie.dungeonhub.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("me.taubsie.dungeonhub.*")
public class DungeonHubServerApplicationDev {
    public static void main(String[] args) {
        SpringApplication.run(DungeonHubServerApplicationDev.class, args);
    }
}