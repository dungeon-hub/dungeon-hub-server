package me.taubsie.dungeonhub.server.changes;

import net.dungeonhub.model.change.ChangeEventModel;

/**
 * A change that is published within the application and then sent to every subscriber of the change stream that is
 * part of the {@link #audience()}.
 */
public record ChangeEvent(ChangeEventModel<?> model, ChangeAudience audience) {
}
