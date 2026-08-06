package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StaticMessageObjectId implements Serializable {
    @Column(name = "static_message")
    private Long staticMessageId;

    @Column(name = "object_id")
    private Long objectId;
}