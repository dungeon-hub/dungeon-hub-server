package me.taubsie.carrylogs.server.controller;

import me.taubsie.carrylogs.server.service.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController
{
    private final TokenService tokenService;

    public AuthController(TokenService tokenService)
    {
        this.tokenService = tokenService;
    }

    //TODO get spring logger

    @GetMapping("/token")
    public String token(Authentication authentication)
    {
        System.out.println("Token requested for user: '" + authentication.getName() + "'");
        String token = tokenService.generateToken(authentication);
        System.out.println("Token granted " + token);
        return token;
    }
}