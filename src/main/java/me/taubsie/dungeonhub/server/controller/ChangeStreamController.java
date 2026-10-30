package me.taubsie.dungeonhub.server.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.changes.ChangeStreamService;
import net.dungeonhub.enums.ChangeEventType;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/changes")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Change-Stream")
public class ChangeStreamController {
    private final ChangeStreamService changeStreamService;

    /**
     * Server-sent event stream that pushes changes of entities to the client, as long as the client is allowed to see them.
     * The name of each event is the type of the changed entity, the data is a change event model.
     * Clients should ignore events with unknown names. The stream is closed once the used token expires.
     * Changes that happen while a client isn't connected aren't replayed.
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChanges(
            Authentication authentication,
            @RequestParam(value = "types", required = false) Set<ChangeEventType> types,
            HttpServletResponse response
    ) {
        // prevents reverse proxies like nginx from buffering the events
        response.setHeader("X-Accel-Buffering", "no");

        return changeStreamService.subscribe(authentication, types == null ? Set.of() : types);
    }
}
