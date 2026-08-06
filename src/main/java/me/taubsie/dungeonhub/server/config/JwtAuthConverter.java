package me.taubsie.dungeonhub.server.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt), extractName(jwt));
    }

    private List<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
        if (!jwt.hasClaim("groups") && !jwt.hasClaim("permissions")) {
            return List.of();
        }

        return extractClaims(jwt)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private Stream<String> extractClaims(Jwt jwt) {
        return Stream.concat(
                jwt.getClaimAsStringList("groups") != null
                        ? jwt.getClaimAsStringList("groups").stream().map(groupName -> "ROLE_" + groupName)
                        : Stream.empty(),
                jwt.getClaimAsStringList("permissions") != null
                        ? jwt.getClaimAsStringList("permissions").stream()
                        : Stream.empty()
        );
    }

    private String extractName(Jwt jwt) {
        String name = jwt.getClaimAsString("preferred_username");

        return name != null ? name : jwt.getSubject();
    }
}