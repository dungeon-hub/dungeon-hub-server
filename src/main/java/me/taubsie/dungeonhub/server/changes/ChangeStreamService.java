package me.taubsie.dungeonhub.server.changes;

import jakarta.annotation.PreDestroy;
import me.taubsie.dungeonhub.server.service.AuthenticationService;
import net.dungeonhub.enums.ChangeEventType;
import net.dungeonhub.model.change.ChangeStreamConnectedModel;
import org.jspecify.annotations.Nullable;
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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Keeps track of all clients connected to the change stream and sends them the changes they are allowed to see.
 * <p>
 * Every subscriber has its own sending thread, so it receives the changes in the order they were published and a
 * slow client doesn't delay the others. Recently published changes are kept for a while, so clients that reconnect
 * (e.g. because their token expired) can resume the stream without missing a change.
 */
@Service
public class ChangeStreamService {
    static final long HEARTBEAT_INTERVAL_MILLIS = 25_000;
    static final Duration RESUMABLE_DURATION = Duration.ofMinutes(10);
    static final int MAX_RESUMABLE_CHANGES = 10_000;

    private static final Logger logger = LoggerFactory.getLogger(ChangeStreamService.class);

    private final AuthenticationService authenticationService;

    /**
     * Event ids are only valid for this instance of the server, as the sequence restarts with it.
     */
    private final String streamId = UUID.randomUUID().toString();

    // all fields below are guarded by the lock, so that changes are sent and stored in the same order
    private final Object lock = new Object();
    private final Map<ChangeSubscriber, ExecutorService> subscribers = new HashMap<>();
    private final Deque<PublishedChange> recentChanges = new ArrayDeque<>();
    private long lastSequence = 0;

    public ChangeStreamService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Registers a new client, which from now on receives all changes it is allowed to see.
     * The connection is closed once the token used to authenticate expires, as the authorities of the client might
     * have changed until then. Clients are expected to reconnect with a new token and the id of the last event they
     * received, so that they receive the changes they missed in the meantime.
     *
     * @param lastEventId the id of the last event the client received before, if it is reconnecting
     */
    public SseEmitter subscribe(Authentication authentication, Set<ChangeEventType> types, @Nullable String lastEventId) {
        SseEmitter emitter = new SseEmitter(getConnectionTimeout(authentication).toMillis());

        ChangeSubscriber subscriber = new ChangeSubscriber(
                emitter,
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()),
                authenticationService.getLoggedInDiscordId(authentication).orElse(null),
                types
        );

        emitter.onCompletion(() -> unsubscribe(subscriber));
        emitter.onError(throwable -> unsubscribe(subscriber));
        emitter.onTimeout(emitter::complete);

        synchronized (lock) {
            subscribers.put(subscriber, Executors.newSingleThreadExecutor(Thread.ofVirtual().name("change-stream-", 0).factory()));

            List<PublishedChange> missedChanges = getMissedChanges(lastEventId);

            // sending something right away also commits the response, which tells the client that it is subscribed now
            deliver(subscriber, () -> SseEmitter.event()
                    .name(ChangeStreamConnectedModel.EVENT_NAME)
                    .data(new ChangeStreamConnectedModel(missedChanges != null), MediaType.APPLICATION_JSON));

            if (missedChanges != null) {
                missedChanges.stream()
                        .filter(change -> subscriber.shouldReceive(change.event()))
                        .forEach(change -> deliver(subscriber, change::toSseEvent));
            }
        }

        return emitter;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onChange(ChangeEvent event) {
        synchronized (lock) {
            long sequence = ++lastSequence;
            PublishedChange change = new PublishedChange(streamId + ":" + sequence, sequence, Instant.now(), event);

            recentChanges.addLast(change);
            removeOutdatedChanges();

            subscribers.keySet().stream()
                    .filter(subscriber -> subscriber.shouldReceive(event))
                    .forEach(subscriber -> deliver(subscriber, change::toSseEvent));
        }
    }

    /**
     * Regularly sends a comment to all clients, so that neither the clients nor proxies in between consider the
     * connection dead, and so that connections of clients that disconnected are cleaned up.
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MILLIS, initialDelay = HEARTBEAT_INTERVAL_MILLIS)
    public void sendHeartbeat() {
        synchronized (lock) {
            removeOutdatedChanges();
            subscribers.keySet().forEach(subscriber -> deliver(subscriber, () -> SseEmitter.event().comment("heartbeat")));
        }
    }

    /**
     * @return the changes published after the given event, or null if they can't be determined, e.g. because the
     * event is unknown or some of the changes after it were already removed
     */
    private @Nullable List<PublishedChange> getMissedChanges(@Nullable String lastEventId) {
        if (lastEventId == null || !lastEventId.startsWith(streamId + ":")) {
            return null;
        }

        long lastReceivedSequence;
        try {
            lastReceivedSequence = Long.parseLong(lastEventId.substring(streamId.length() + 1));
        } catch (NumberFormatException exception) {
            return null;
        }

        long oldestKnownSequence = recentChanges.isEmpty() ? lastSequence + 1 : recentChanges.getFirst().sequence();
        if (lastReceivedSequence > lastSequence || lastReceivedSequence + 1 < oldestKnownSequence) {
            return null;
        }

        return recentChanges.stream()
                .filter(change -> change.sequence() > lastReceivedSequence)
                .toList();
    }

    private void removeOutdatedChanges() {
        Instant oldestKeptTime = Instant.now().minus(RESUMABLE_DURATION);

        while (!recentChanges.isEmpty() && (recentChanges.size() > MAX_RESUMABLE_CHANGES
                || recentChanges.getFirst().time().isBefore(oldestKeptTime))) {
            recentChanges.removeFirst();
        }
    }

    /**
     * Queues the event for the subscriber. The event is built by the sending thread, as an event can only be sent once.
     */
    private void deliver(ChangeSubscriber subscriber, Supplier<SseEmitter.SseEventBuilder> event) {
        ExecutorService executor = subscribers.get(subscriber);
        if (executor == null) {
            return;
        }

        try {
            executor.execute(() -> send(subscriber, event.get()));
        } catch (RejectedExecutionException exception) {
            // the subscriber was removed in the meantime
        }
    }

    private void send(ChangeSubscriber subscriber, SseEmitter.SseEventBuilder event) {
        try {
            subscriber.emitter().send(event);
        } catch (IOException | IllegalStateException exception) {
            // the client disconnected or the connection was already completed, the servlet container cleans up the rest
            logger.debug("Couldn't send a change to a subscriber, removing it.", exception);
            unsubscribe(subscriber);
        }
    }

    private void unsubscribe(ChangeSubscriber subscriber) {
        synchronized (lock) {
            ExecutorService executor = subscribers.remove(subscriber);
            if (executor != null) {
                executor.shutdown();
            }
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
        synchronized (lock) {
            subscribers.forEach((subscriber, executor) -> {
                executor.shutdown();
                subscriber.emitter().complete();
            });
            subscribers.clear();
        }
    }

    private record PublishedChange(String id, long sequence, Instant time, ChangeEvent event) {
        SseEmitter.SseEventBuilder toSseEvent() {
            return SseEmitter.event()
                    .id(id)
                    .name(event.model().getType().name())
                    .data(event.model(), MediaType.APPLICATION_JSON);
        }
    }
}
