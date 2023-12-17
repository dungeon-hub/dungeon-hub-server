package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenId implements Serializable {
    @Column(name = "user", nullable = false)
    private Long user;

    @Column(name = "token", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID token;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshTokenId that = (RefreshTokenId) o;
        return Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}