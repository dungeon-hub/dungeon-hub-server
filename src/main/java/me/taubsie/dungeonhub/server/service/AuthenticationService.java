package me.taubsie.dungeonhub.server.service;

import jakarta.annotation.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationService {
    private static final String DISCORD_ID_CLAIM = "discord-id";

    @Nullable
    public Long getLoggedInDiscordId(Authentication authentication) {
        if(!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        Map<String, Object> claims = jwt.getClaims();
        if(!(claims.get(DISCORD_ID_CLAIM) instanceof Long userId) || userId <= 0) {
            return null;
        }

        return userId;
    }
}
