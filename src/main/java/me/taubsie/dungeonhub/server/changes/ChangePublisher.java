package me.taubsie.dungeonhub.server.changes;

import lombok.AllArgsConstructor;
import net.dungeonhub.enums.ChangeAction;
import net.dungeonhub.enums.ChangeEventType;
import net.dungeonhub.model.change.ChangeEventModel;
import net.dungeonhub.structure.model.Model;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Entry point for services to announce changes of their entities to the clients of the change stream.
 * <p>
 * Changes are delivered after the surrounding transaction was committed, or immediately if there is none.
 */
@Component
@AllArgsConstructor
public class ChangePublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * @param type     the type of the changed entity
     * @param action   what happened to the entity
     * @param serverId the server the entity belongs to, null if it doesn't belong to one
     * @param data     the model of the entity after the change, or its last state if it was deleted
     * @param audience who is allowed to see the change
     */
    public void publish(ChangeEventType type, ChangeAction action, @Nullable Long serverId, Model data, ChangeAudience audience) {
        if (!type.getModelClass().isInstance(data)) {
            throw new IllegalArgumentException("The data of a " + type + " change has to be a " + type.getModelClass().getSimpleName()
                    + ", but was a " + data.getClass().getSimpleName() + ".");
        }

        applicationEventPublisher.publishEvent(new ChangeEvent(new ChangeEventModel<>(type, action, serverId, data), audience));
    }
}
