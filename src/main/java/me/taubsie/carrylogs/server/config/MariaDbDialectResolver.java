package me.taubsie.carrylogs.server.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jdbc.repository.config.DialectResolver;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.MySqlDialect;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.Optional;

public class MariaDbDialectResolver implements DialectResolver.JdbcDialectProvider
{
    @Override
    public @NotNull Optional<Dialect> getDialect(@NotNull JdbcOperations jdbcOperations) {
        return Optional.of(MySqlDialect.INSTANCE);
    }
}