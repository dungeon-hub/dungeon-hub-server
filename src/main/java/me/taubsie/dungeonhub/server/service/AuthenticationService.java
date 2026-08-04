package me.taubsie.dungeonhub.server.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthenticationService {
    private static final String DISCORD_ID_CLAIM = "discord-id";

    public Optional<Long> getLoggedInDiscordId(@NotNull Authentication authentication) {
        if(!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }

        Map<String, Object> claims = jwt.getClaims();
        if(!(claims.get(DISCORD_ID_CLAIM) instanceof Long userId) || userId <= 0) {
            return Optional.empty();
        }

        return Optional.of(userId);
    }
}
