package me.taubsie.dungeonhub.server.security.exception;

import lombok.Getter;
import me.taubsie.dungeonhub.server.exception.OccupiedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class LoginNameOccupiedException extends OccupiedException {
}