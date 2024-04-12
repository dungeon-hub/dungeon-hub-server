package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.enums.ScoreType;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ScoreId implements Serializable {
    @Serial
    private static final long serialVersionUID = -242796128838012391L;

    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "carry_type", nullable = false)
    private Long carryType;

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

        if (object instanceof ScoreId scoreId) {
            return Objects.equals(this.id, scoreId.id) &&
                    Objects.equals(this.carryType, scoreId.carryType) &&
                    Objects.equals(this.scoreType, scoreId.scoreType);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, carryType, scoreType);
    }
}