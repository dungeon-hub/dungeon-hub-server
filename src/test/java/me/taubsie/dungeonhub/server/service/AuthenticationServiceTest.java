package me.taubsie.dungeonhub.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthenticationServiceTest {
    private final AuthenticationService authenticationService = new AuthenticationService();

    @Test
    void returnsPositiveDiscordId() {
        long discordId = 123456789L;

        Long result = authenticationService.getLoggedInDiscordId(authenticationWithDiscordId(discordId));

        assertEquals(discordId, result);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, Long.MIN_VALUE})
    void rejectsNonPositiveDiscordId(long discordId) {
        Long result = authenticationService.getLoggedInDiscordId(authenticationWithDiscordId(discordId));

        assertNull(result);
    }

    private JwtAuthenticationToken authenticationWithDiscordId(long discordId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("discord-id", discordId)
                .build();

        return new JwtAuthenticationToken(jwt);
    }
}
