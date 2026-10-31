package me.taubsie.dungeonhub.server.changes;

import net.dungeonhub.enums.ChangeEventType;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

/**
 * A client that is connected to the change stream.
 *
 * @param emitter     the connection to the client
 * @param authorities the authorities of the client at the time it subscribed
 * @param discordId   the discord id of the logged-in user, if there is one
 * @param types       the types of changes the client wants to receive, all types if empty
 */
record ChangeSubscriber(SseEmitter emitter, Set<String> authorities, @Nullable Long discordId, Set<ChangeEventType> types) {
    private static final Set<String> PRIVILEGED_AUTHORITIES = Set.of("ROLE_bot", "ROLE_admin");
    private static final String SERVER_AUTHORITY_PREFIX = "server_";

    ChangeSubscriber {
        authorities = Set.copyOf(authorities);
        types = Set.copyOf(types);
    }

    boolean shouldReceive(ChangeEvent event) {
        return (types.isEmpty() || types.contains(event.model().getType())) && canSee(event.audience());
    }

    private boolean canSee(ChangeAudience audience) {
        if (authorities.stream().anyMatch(PRIVILEGED_AUTHORITIES::contains)) {
            return true;
        }

        if (audience.serverId() != null && authorities.contains(SERVER_AUTHORITY_PREFIX + audience.serverId())) {
            return true;
        }

        return discordId != null && audience.userIds().contains(discordId);
    }
}
