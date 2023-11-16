package me.taubsie.dungeonhub.server.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NameOccupiedException extends OccupiedException
{
    private final String name;
}
