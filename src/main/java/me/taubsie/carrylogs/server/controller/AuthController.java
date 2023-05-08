package me.taubsie.carrylogs.server.controller;

import me.taubsie.carrylogs.server.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController
{
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final TokenService tokenService;

    public AuthController(TokenService tokenService)
    {
        this.tokenService = tokenService;
    }

    @GetMapping("/api/token")
    public String token(Authentication authentication)
    {
        logger.info("Token requested for user: '" + authentication.getName() + "'");
        String token = tokenService.generateToken(authentication);
        logger.debug("Token granted " + token);
        return token;
    }
}