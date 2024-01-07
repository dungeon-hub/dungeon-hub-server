package me.taubsie.dungeonhub.server.config;

import com.google.gson.Gson;
import me.taubsie.dungeonhub.common.DungeonHubService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JsonConfig {
    @Primary
    @Bean
    public Gson gson() {
        return DungeonHubService.getInstance().getGson();
    }
}