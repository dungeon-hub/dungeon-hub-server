package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class PurgeTypeRoleId implements Serializable {
    @Column(name = "discord_role", nullable = false)
    private Long discordRole;

    @Column(name = "purge_type", nullable = false)
    private Long purgeType;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || Hibernate.getClass(this) != Hibernate.getClass(object)) {
            return false;
        }

        if (object instanceof PurgeTypeRoleId purgeTypeRoleId) {
            return Objects.equals(this.discordRole, purgeTypeRoleId.discordRole) &&
                    Objects.equals(this.purgeType, purgeTypeRoleId.purgeType);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(discordRole, purgeType);
    }
}