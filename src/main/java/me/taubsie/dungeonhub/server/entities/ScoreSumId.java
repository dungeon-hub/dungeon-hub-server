package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.ScoreType;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ScoreSumId implements Serializable {
    @Serial
    private static final long serialVersionUID = -3212043206472304316L;

    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "server", nullable = false)
    private Long server;

    @Enumerated
    @Column(name = "score_type", nullable = false)
    private ScoreType scoreType;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || Hibernate.getClass(this) != Hibernate.getClass(object)) {
            return false;
        }

        if (object instanceof ScoreSumId scoreId) {
            return Objects.equals(this.id, scoreId.id) &&
                    Objects.equals(this.server, scoreId.server) &&
                    Objects.equals(this.scoreType, scoreId.scoreType);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, server, scoreType);
    }
}