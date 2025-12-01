package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.StaticMessageType;
import net.dungeonhub.model.static_message.StaticMessageModel;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "static_message")
@Table(name = "static_message", schema = "dungeon-hub")
@NoArgsConstructor
public class StaticMessage implements net.dungeonhub.structure.entity.Entity<StaticMessageModel> {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server", nullable = false)
    private DiscordServer server;

    @Getter
    @Setter
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Getter
    @Setter
    @Column(name = "message_id")
    private Long messageId;

    @Getter
    @Setter
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "message_type", nullable = false)
    private StaticMessageType staticMessageType;

    @Getter
    @Setter
    @OneToMany(mappedBy = "staticMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private List<StaticMessageObject> staticMessageObjects = new ArrayList<>();

    public void setObjectIds(@NotNull List<Long> objectIds) {
        this.staticMessageObjects.clear();
        this.staticMessageObjects.addAll(objectIds.stream().map(objectId -> new StaticMessageObject(this, objectId)).toList());
    }

    public StaticMessage(DiscordServer server, Long channelId, Long messageId, StaticMessageType staticMessageType, @NotNull List<Long> objectIds) {
        this.server = server;
        this.channelId = channelId;
        this.messageId = messageId;
        this.staticMessageType = staticMessageType;

        this.setObjectIds(objectIds);
    }

    @Override
    public @NonNull StaticMessageModel toModel() {
        return new StaticMessageModel(id, server.toModel(), channelId, messageId, staticMessageType, staticMessageObjects.stream().map(StaticMessageObject::getObjectId).toList());
    }
}

