package me.taubsie.dungeonhub.server.security.encryption;

import org.jetbrains.annotations.Unmodifiable;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

@FunctionalInterface
public interface AuthorityFactory {
    @Unmodifiable Set<? extends GrantedAuthority> get(long id);
}