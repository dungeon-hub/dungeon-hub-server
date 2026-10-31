package me.taubsie.dungeonhub.server.changes;

import net.dungeonhub.enums.ChangeAction;
import net.dungeonhub.enums.ChangeEventType;
import net.dungeonhub.model.change.ChangeEventModel;
import net.dungeonhub.model.score.ScoreModel;
import net.dungeonhub.model.discord_user.DiscordUserModel;
import net.dungeonhub.enums.ScoreType;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangeSubscriberTest {
    private static final long SERVER_ID = 1L;
    private static final long USER_ID = 42L;

    @Test
    void privilegedSubscribersReceiveEverything() {
        ChangeEvent privilegedEvent = event(ChangeAudience.privileged());

        assertTrue(subscriber(Set.of("ROLE_bot"), null).shouldReceive(privilegedEvent));
        assertTrue(subscriber(Set.of("ROLE_admin"), null).shouldReceive(privilegedEvent));
    }

    @Test
    void serverManagersOnlyReceiveChangesOfTheirServer() {
        ChangeSubscriber subscriber = subscriber(Set.of("server_" + SERVER_ID), null);

        assertTrue(subscriber.shouldReceive(event(ChangeAudience.server(SERVER_ID))));
        assertFalse(subscriber.shouldReceive(event(ChangeAudience.server(SERVER_ID + 1))));
        assertFalse(subscriber.shouldReceive(event(ChangeAudience.privileged())));
    }

    @Test
    void usersOnlyReceiveChangesTheyArePartOf() {
        ChangeSubscriber subscriber = subscriber(Set.of(), USER_ID);

        assertTrue(subscriber.shouldReceive(event(ChangeAudience.server(SERVER_ID).withUsers(List.of(USER_ID)))));
        assertFalse(subscriber.shouldReceive(event(ChangeAudience.server(SERVER_ID).withUsers(List.of(USER_ID + 1)))));
        assertFalse(subscriber.shouldReceive(event(ChangeAudience.server(SERVER_ID))));
    }

    @Test
    void subscribersWithoutDiscordIdDontReceiveUserChanges() {
        assertFalse(subscriber(Set.of(), null).shouldReceive(event(ChangeAudience.privileged().withUsers(List.of(USER_ID)))));
    }

    @Test
    void subscribersOnlyReceiveTheTypesTheyAskedFor() {
        ChangeEvent scoreChange = event(ChangeAudience.privileged());

        assertTrue(new ChangeSubscriber(new SseEmitter(), Set.of("ROLE_bot"), null, Set.of(ChangeEventType.Score)).shouldReceive(scoreChange));
        assertFalse(new ChangeSubscriber(new SseEmitter(), Set.of("ROLE_bot"), null, Set.of(ChangeEventType.Ticket)).shouldReceive(scoreChange));
    }

    private static ChangeSubscriber subscriber(Set<String> authorities, Long discordId) {
        return new ChangeSubscriber(new SseEmitter(), authorities, discordId, Set.of());
    }

    private static ChangeEvent event(ChangeAudience audience) {
        ScoreModel score = new ScoreModel(new DiscordUserModel(USER_ID, null, null), null, ScoreType.Default, 1L);

        return new ChangeEvent(new ChangeEventModel<>(ChangeEventType.Score, ChangeAction.Updated, SERVER_ID, score), audience);
    }
}
