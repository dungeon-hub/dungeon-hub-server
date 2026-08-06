package me.taubsie.dungeonhub.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/")
public class DungeonHubRestController {
    @GetMapping("hello")
    public ResponseEntity<String> hello(Principal principal) {
        return new ResponseEntity<>(String.format("Hello, %s!", principal.getName()), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @GetMapping("admin")
    public ResponseEntity<String> admin(Principal principal) {
        return new ResponseEntity<>(String.format("Hello, %s! Welcome to the secret endpoint.", principal.getName()),
                HttpStatus.OK);
    }
}