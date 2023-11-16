package me.taubsie.dungeonhub.server.security;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.model.security.user.UserCreationModel;
import me.taubsie.dungeonhub.common.model.security.user.UserLoginModel;
import me.taubsie.dungeonhub.common.model.security.user.UserLoginVerificationModel;
import me.taubsie.dungeonhub.server.security.user.UserService;
import me.taubsie.dungeonhub.server.security.user.model.UserInitializeModel;
import me.taubsie.dungeonhub.server.security.user.model.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/user")
@AllArgsConstructor
@Tag(name = "API-Key")
public class UserController {
    private final UserService userService;

    @Hidden
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    public UserModel createUser(@RequestBody UserCreationModel userCreationModel) {
        UserInitializeModel userInitializeModel = new UserInitializeModel().fromCreationModel(userCreationModel);

        return userService.create(userInitializeModel);
    }

    @GetMapping("me")
    public UserModel getUserData(Authentication authentication) {
        return userService.loadById(Long.parseLong(authentication.getName()))
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }

    @PermitAll
    @PostMapping("login")
    public UserLoginVerificationModel loginUser(@RequestBody UserLoginModel userLoginModel) {
        return userService.login(userLoginModel)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
    }
}