package me.taubsie.dungeonhub.server.config;

import com.google.gson.Gson;
import net.dungeonhub.service.GsonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JsonConfig {
    @Primary
    @Bean
    public Gson gson() {
        //unfortunately we have to use gson here, since Moshi isn't supported
        //noinspection deprecation
        return GsonService.INSTANCE.getGson();
    }
}