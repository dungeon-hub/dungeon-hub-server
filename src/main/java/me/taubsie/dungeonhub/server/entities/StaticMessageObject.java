package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity(name = "static_message_object")
@Table(name = "static_message_object", schema = "dungeon-hub")
@NoArgsConstructor
public class StaticMessageObject {
    @EmbeddedId
    private StaticMessageObjectId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("staticMessageId")
    @JoinColumn(name = "static_message")
    private StaticMessage staticMessage;

    public Long getObjectId() {
        return id.getObjectId();
    }

    public StaticMessageObject(StaticMessage staticMessage, Long objectId) {
        this.staticMessage = staticMessage;
        this.id = new StaticMessageObjectId(staticMessage.getId(), objectId);
    }
}