package me.taubsie.dungeonhub.server.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
    @Bean
    @NotNull
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}