package me.taubsie.dungeonhub.server.exception;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class ProgramStartException extends RuntimeException {

    public ProgramStartException(@NotNull String message) {
        super(message);
    }

    public ProgramStartException(@Nullable Throwable cause) {
        super(cause);
    }

    public ProgramStartException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}