package me.taubsie.dungeonhub.server.changes;

import jakarta.annotation.PreDestroy;
import me.taubsie.dungeonhub.server.service.AuthenticationService;
import net.dungeonhub.enums.ChangeEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Keeps track of all clients connected to the change stream and sends them the changes they are allowed to see.
 * <p>
 * All messages are sent from a single thread, so every client receives the changes in the order they were published.
 */
@Service
public class ChangeStreamService {
    private static final Logger logger = LoggerFactory.getLogger(ChangeStreamService.class);
    static final long HEARTBEAT_INTERVAL_MILLIS = 25_000;

    private final AuthenticationService authenticationService;
    private final Set<ChangeSubscriber> subscribers = ConcurrentHashMap.newKeySet();
    private final ExecutorService dispatcher = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "change-stream-dispatcher"));

    public ChangeStreamService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Registers a new client, which from now on receives all changes it is allowed to see.
     * The connection is closed once the token used to authenticate expires, as the authorities of the client might
     * have changed until then. Clients are expected to reconnect with a new token.
     */
    public SseEmitter subscribe(Authentication authentication, Set<ChangeEventType> types) {
        SseEmitter emitter = new SseEmitter(getConnectionTimeout(authentication).toMillis());

        ChangeSubscriber subscriber = new ChangeSubscriber(
                emitter,
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()),
                authenticationService.getLoggedInDiscordId(authentication).orElse(null),
                types
        );

        emitter.onCompletion(() -> subscribers.remove(subscriber));
        emitter.onError(throwable -> subscribers.remove(subscriber));
        emitter.onTimeout(emitter::complete);

        subscribers.add(subscriber);

        // sending something right away commits the response, which tells the client that it is subscribed from now on
        dispatcher.execute(() -> send(subscriber, SseEmitter.event().comment("connected")));

        return emitter;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onChange(ChangeEvent event) {
        dispatcher.execute(() -> subscribers.stream()
                .filter(subscriber -> subscriber.shouldReceive(event))
                .forEach(subscriber -> send(subscriber, SseEmitter.event()
                        .name(event.model().getType().name())
                        .data(event.model(), MediaType.APPLICATION_JSON))));
    }

    /**
     * Regularly sends a comment to all clients, so that neither the clients nor proxies in between consider the
     * connection dead, and so that connections of clients that disconnected are cleaned up.
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MILLIS, initialDelay = HEARTBEAT_INTERVAL_MILLIS)
    public void sendHeartbeat() {
        dispatcher.execute(() -> subscribers.forEach(subscriber -> send(subscriber, SseEmitter.event().comment("heartbeat"))));
    }

    private void send(ChangeSubscriber subscriber, SseEmitter.SseEventBuilder event) {
        try {
            subscriber.emitter().send(event);
        } catch (IOException | IllegalStateException exception) {
            // the client disconnected or the connection was already completed, the servlet container cleans up the rest
            logger.debug("Couldn't send a change to a subscriber, removing it.", exception);
            subscribers.remove(subscriber);
        }
    }

    private Duration getConnectionTimeout(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt && jwt.getExpiresAt() != null) {
            Duration untilExpiry = Duration.between(Instant.now(), jwt.getExpiresAt());

            return untilExpiry.isNegative() || untilExpiry.isZero() ? Duration.ofMillis(1) : untilExpiry;
        }

        // without an expiring token the connection is kept open until the client disconnects
        return Duration.ZERO;
    }

    @PreDestroy
    public void shutdown() {
        dispatcher.shutdown();
        subscribers.forEach(subscriber -> subscriber.emitter().complete());
        subscribers.clear();
    }
}
