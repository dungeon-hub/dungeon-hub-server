package me.taubsie.dungeonhub.server.changes;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Describes which clients are allowed to see a change.
 * Clients with the role {@code bot} or {@code admin} are always allowed to see every change.
 *
 * @param serverId if set, clients that are allowed to manage this server (authority {@code server_<id>}) can see the change
 * @param userIds  the discord ids of users that can see the change, e.g. the claimer of a ticket
 */
public record ChangeAudience(@Nullable Long serverId, Set<Long> userIds) {
    public ChangeAudience {
        userIds = Set.copyOf(userIds);
    }

    /**
     * Only clients with the role {@code bot} or {@code admin} can see the change.
     */
    public static ChangeAudience privileged() {
        return new ChangeAudience(null, Set.of());
    }

    /**
     * Clients that are allowed to manage the given server can see the change, the same as for the server-specific REST endpoints.
     */
    public static ChangeAudience server(long serverId) {
        return new ChangeAudience(serverId, Set.of());
    }

    /**
     * Returns a copy of this audience, in which the given users can additionally see the change.
     * {@code null} values are ignored, so optional users can be passed directly.
     */
    public ChangeAudience withUsers(Collection<@Nullable Long> additionalUserIds) {
        Set<Long> users = new HashSet<>(userIds);
        additionalUserIds.stream().filter(Objects::nonNull).forEach(users::add);

        return new ChangeAudience(serverId, users);
    }
}
